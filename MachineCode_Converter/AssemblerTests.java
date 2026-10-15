package MachineCode_Converter;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AssemblerTests {
    //get path
    static final Path ROOT = Paths.get(".").toAbsolutePath().normalize();
    //make folder for test only
    static final Path WORK = ROOT.resolve("test");
    static final Path DATA_DIR = WORK.resolve("MachineCode_Converter");

    static final Path IN_FILE = DATA_DIR.resolve("assembly-code-file");
    static final Path OUT_FILE = DATA_DIR.resolve("machine-code-file");

    record Test(String name, String asm, List<String> expectedLines, String expectedError) {
    }

    static String asm(String... lines) {
        return String.join("\n", lines) + "\n";
    }

    static Test ok(String name, String asm, String... expected) {
        return new Test(name, asm, Arrays.asList(expected), null);
    }

    static Test err(String name, String asm, String message) {
        return new Test(name, asm, null, message);
    }

    static List<Test> tests() {
        List<Test> t = new ArrayList<>();

        t.add(ok("classic example",
                asm("        lw      0   1   five",
                        "        lw      1   2   3",
                        "start   add     1   2   1",
                        "        beq     0   1   2",
                        "        beq     0   0   start",
                        "        noop",
                        "done    halt",
                        "five    .fill   5",
                        "neg1    .fill   -1",
                        "stAddr  .fill   start"),
                "8454151", "9043971", "655361", "16842754", "16842749",
                "29360128", "25165824", "5", "-1", "2"));

        t.add(ok("all opcodes, comments, blank line",
                asm("        add     1   2   3       # comment",
                        "        nand    4   5   6",
                        "        lw      1   2   data",
                        "        sw      3   4   -1",
                        "loop    beq     1   1   end",
                        "        jalr    2   3           # comment",
                        "        noop",
                        "",
                        "end     halt",
                        "data    .fill   32767",
                        "        .fill   -32768"),
                "655363", "6619142", "9043976", "14483455", "17367042",
                "22216704", "29360128", "25165824", "32767", "-32768"));

        t.add(ok(".fill accepts 32-bit values",
                asm("        .fill 70000",
                        "        .fill 2147483647",
                        "        .fill -2147483648"),
                "70000", "2147483647", "-2147483648"));

        t.add(err("duplicate label",
                asm("a       add 0 0 0", "a       halt"),
                "error: line 2: duplicate label a"));
        t.add(err("label too long",
                asm("toolong halt"),
                "error: line 1: invalid label toolong"));
        t.add(err("undefined label",
                asm("        lw 0 1 nowhere"),
                "error: line 1: undefined label nowhere"));
        t.add(err("unrecognized opcode",
                asm("        addi 1 2 3"),
                "error: line 1: unrecognized opcode addi"));
        t.add(err("lw offset too large",
                asm("        lw 0 1 40000"),
                "error: value out of range: 40000"));
        t.add(err("lw offset just over limit",
                asm("        lw 0 1 32768"),
                "error: value out of range: 32768"));
        t.add(err("beq offset too large",
                asm("        beq 0 0 40000"),
                "error: offset out of range: 40000"));
        t.add(err("comment in column 0 read as label",
                asm("# hello"),
                "error: line 1: invalid label #"));
        t.add(err("missing operand",
                asm("        add 1 2"),
                "error: line 1: missing operand"));
        t.add(err("missing .fill operand",
                asm("        .fill"),
                "error: line 1: missing operand"));
        t.add(err("register too large",
                asm("        add 8 0 0"),
                "error: line 1: invalid register 8"));
        t.add(err("negative register",
                asm("        add -1 0 0"),
                "error: line 1: invalid register -1"));
        t.add(err("non-numeric register",
                asm("        jalr a b"),
                "error: line 1: invalid register a"));

        return t;
    }

    record Result(int exitCode, String output, boolean timedOut) {
    }

    static String absoluteClassPath() {
        List<String> parts = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            parts.add(Paths.get(entry).toAbsolutePath().normalize().toString());
        }
        return String.join(File.pathSeparator, parts);
    }

    static void cleanUp() throws IOException {
        Files.deleteIfExists(IN_FILE);
        Files.deleteIfExists(OUT_FILE);
        for (Path dir : new Path[]{DATA_DIR, WORK}) {
            try {
                Files.deleteIfExists(dir);
            } catch (DirectoryNotEmptyException e) {
                //
            }
        }
    }

    static Result runAssembler() throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().contains("--enable-preview")) {
            cmd.add("--enable-preview");
        }
        cmd.add("-cp");
        cmd.add(absoluteClassPath());
        cmd.add("AssemblyReader");

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(WORK.toFile());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!p.waitFor(30, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            return new Result(-1, output, true);
        }
        return new Result(p.exitValue(), output, false);
    }

    static List<String> check(Test t, Result r) throws IOException {
        List<String> problems = new ArrayList<>();
        if (r.timedOut()) {
            problems.add("timed out");
            return problems;
        }
        boolean outExists = Files.exists(OUT_FILE);

        if (t.expectedError() == null) {
            if (r.exitCode() != 0) {
                problems.add("exit code " + r.exitCode() + ": " + r.output().strip());
            } else if (!outExists) {
                problems.add("no output file written");
            } else {
                List<String> got = new ArrayList<>();
                for (String line : Files.readAllLines(OUT_FILE)) {
                    if (!line.isBlank()) {
                        got.add(line.trim());
                    }
                }
                if (!got.equals(t.expectedLines())) {
                    problems.add("expected " + t.expectedLines() + " but got " + got);
                }
            }
        } else {
            if (r.exitCode() != 1) {
                problems.add("expected exit code 1, got " + r.exitCode());
            }
            if (!r.output().contains(t.expectedError())) {
                problems.add("expected \"" + t.expectedError() + "\" but got \"" + r.output().strip() + "\"");
            }
            if (outExists) {
                problems.add("output file written despite error");
            }
        }
        return problems;
    }

    public static void main(String[] args) throws Exception {
        boolean keep = Arrays.asList(args).contains("--keep");
        Files.createDirectories(DATA_DIR);

        List<Test> all = tests();
        int failed = 0;

        for (Test t : all) {
            Files.deleteIfExists(OUT_FILE);
            Files.writeString(IN_FILE, t.asm());
            List<String> problems = check(t, runAssembler());
            if (!problems.isEmpty()) {
                failed++;
                System.out.println("FAIL: " + t.name());
                problems.forEach(p -> System.out.println("  " + p));
            }
        }

        if (!keep) {
            cleanUp();
        }

        if (failed == 0) {
            System.out.println("All " + all.size() + " tests passed.");
        } else {
            System.out.println(failed + " of " + all.size() + " tests failed.");
            System.exit(1);
        }
    }
}