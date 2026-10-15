import java.io.*;
import java.util.*;
import java.util.List;

import static java.lang.System.exit;

public class Assembler {

    static final int MAXLINELENGTH = 1000;
    static final int LABEL = 0;
    static final int OPCODE = 1;
    static final int ARG0 = 2;
    static final int ARG1 = 3;
    static final int ARG2 = 4;

    static boolean readAndParse(AsmReader in, String[] fields) {
        Arrays.fill(fields, "");

        String line = in.readLine();
        if (line == null) {
            return false;
        }

        if (line.length() >= MAXLINELENGTH - 1) {
            System.out.println("error: line too long");
            exit(1);
        }

        String rest = line;
        if (!line.isEmpty() && !Character.isWhitespace(line.charAt(0))) {
            int end = 0;
            while (end < line.length() && !Character.isWhitespace(line.charAt(end))) {
                end++;
            }
            fields[LABEL] = line.substring(0, end);
            rest = line.substring(end);
        }

        String trimmed = rest.trim();
        if (!trimmed.isEmpty()) {
            String[] tokens = trimmed.split("\\s+");
            for (int i = 0; i < 4 && i < tokens.length; i++) {
                fields[OPCODE + i] = tokens[i];
            }
        }
        return true;
    }

    static boolean isNumber(String string) {
        try {
            Integer.parseInt(string.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static class AsmReader {
        private final List<String> lines = new ArrayList<>();
        private int pos = 0;

        AsmReader(String path) throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String l;
                while ((l = br.readLine()) != null) {
                    lines.add(l);
                }
            }
        }

        String readLine() {
            return pos < lines.size() ? lines.get(pos++) : null;
        }

        void rewind() {
            pos = 0;
        }
    }

    static final Set<String> OPCODES = Set.of(
            "add", "nand", "lw", "sw", "beq", "jalr", "halt", "noop", ".fill");

    static boolean ErrorChecker(Map<Integer, String[]> Assembly) {
        Collector c = new Collector(Assembly);
        if (!c.ok) {
            return false;
        }

        for (Map.Entry<Integer, String[]> e : Assembly.entrySet()) {
            int lineNo = e.getKey();
            String[] f = e.getValue();
            String op = f[OPCODE];

            if (op.isEmpty()) {
                continue;
            }
            if (!OPCODES.contains(op)) {
                System.out.println("error: line " + lineNo + ": unrecognized opcode " + op);
                return false;
            }

            if (op.equals("lw") || op.equals("sw") || op.equals("beq")) {
                String a2 = c.arg2.get(lineNo);
                if (!isNumber(a2) && !c.labels.containsKey(a2)) {
                    System.out.println("error: line " + lineNo + ": undefined label " + a2);
                    return false;
                }
            }

            if (op.equals(".fill")) {
                String a0 = f[ARG0];
                if (!isNumber(a0) && !c.labels.containsKey(a0)) {
                    System.out.println("error: line " + lineNo + ": undefined label " + a0);
                    return false;
                }
            }
        }
        return true;
    }

    static class Collector {
        HashMap<String, Integer> labels = new HashMap<>();
        HashMap<Integer, String> arg2 = new HashMap<>();
        boolean ok = true;

        Collector(Map<Integer, String[]> Assembly) {
            for (Map.Entry<Integer, String[]> e : Assembly.entrySet()) {
                int lineNo = e.getKey();
                String[] f = e.getValue();
                String label = f[LABEL];

                if (!label.isEmpty()) {
                    if (!label.matches("[A-Za-z][A-Za-z0-9]{0,5}")) {
                        System.out.println("error: line " + lineNo + ": invalid label " + label);
                        ok = false;
                        return;
                    }
                    if (labels.containsKey(label)) {
                        System.out.println("error: line " + lineNo + ": duplicate label " + label);
                        ok = false;
                        return;
                    }
                    labels.put(label, lineNo);
                }
                arg2.put(lineNo, f[ARG2]);
            }
        }
    }

    static class ConvertMachineCode {
        TreeMap<Integer, Integer> assembly_machinecode = new TreeMap<>();
        Map<String, Integer> addressOf = new HashMap<>();

        ConvertMachineCode(TreeMap<Integer, String[]> assembly) {
            int addr = 0;
            for (String[] v : assembly.values()) {
                if (v[OPCODE].isEmpty()) {
                    continue;
                }
                if (!v[LABEL].isEmpty()) {
                    addressOf.put(v[LABEL], addr);
                }
                addr++;
            }

            int lineNo = 0;
            for (String[] v : assembly.values()) {
                if (v[OPCODE].isEmpty()) {
                    continue;
                }

                int machinecode = 0;
                int opcode;

                switch (v[OPCODE]) {
                    case ".fill" -> machinecode = resolveValue(v[ARG0]);
                    case "add", "nand" -> {
                        opcode = Objects.equals(v[OPCODE], "add") ? 0 : 1;
                        int arg0 = Integer.parseInt(v[ARG0]);
                        int arg1 = Integer.parseInt(v[ARG1]);
                        int arg2 = Integer.parseInt(v[ARG2]);
                        machinecode = (opcode << 22) | (arg0 << 19) | (arg1 << 16) | arg2;
                    }
                    case "lw", "sw", "beq" -> {
                        if (Objects.equals(v[OPCODE], "lw")) opcode = 2;
                        else if (Objects.equals(v[OPCODE], "sw")) opcode = 3;
                        else opcode = 4;

                        int arg0 = Integer.parseInt(v[ARG0]);
                        int arg1 = Integer.parseInt(v[ARG1]);

                        int offset;
                        if (opcode == 4) {
                            offset = computeRelativeOffset(v[ARG2], lineNo);
                        } else {
                            offset = resolveValue(v[ARG2]);
                        }
                        machinecode = (opcode << 22) | (arg0 << 19) | (arg1 << 16) | (offset & 0xFFFF);
                    }
                    case "jalr" -> {
                        opcode = 5;
                        int arg0 = Integer.parseInt(v[ARG0]);
                        int arg1 = Integer.parseInt(v[ARG1]);
                        machinecode = (opcode << 22) | (arg0 << 19) | (arg1 << 16);
                    }
                    case "halt", "noop" -> {
                        opcode = Objects.equals(v[OPCODE], "halt") ? 6 : 7;
                        machinecode = opcode << 22;
                    }
                }

                assembly_machinecode.put(lineNo, machinecode);
                lineNo++;
            }
        }

        int computeRelativeOffset(String arg2, int currentAddr) {
            if (isNumber(arg2)) {
                int val = Integer.parseInt(arg2.trim());
                if (val < -32768 || val > 32767) {
                    System.out.println("error: offset out of range: " + val);
                    exit(1);
                }
                return val;
            }
            Integer target = addressOf.get(arg2);
            if (target == null) {
                System.out.println("error: undefined label " + arg2);
                exit(1);
            }
            return target - (currentAddr + 1);
        }

        int resolveValue(String arg0) {
            if (isNumber(arg0)) {
                int val = Integer.parseInt(arg0.trim());
                if (val < -32768 || val > 32767) {
                    System.out.println("error: value out of range: " + val);
                    exit(1);
                }
                return val;
            }
            Integer target = addressOf.get(arg0);
            if (target == null) {
                System.out.println("error: undefined label " + arg0);
                exit(1);
            }
            return target;
        }
    }
}