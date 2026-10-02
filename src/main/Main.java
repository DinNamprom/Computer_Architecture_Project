import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

public class Main {
    static final int MAXLINELENGTH = 1000;

    // Indexes into the fields array filled by readAndParse
    static final int LABEL = 0, OPCODE = 1, ARG0 = 2, ARG1 = 3, ARG2 = 4;

    static void main() {

        String inFileString = "D:\\Com Arc\\Proj\\src\\main\\assembly-code-file";
        String outFileString = "D:\\Com Arc\\Proj\\src\\main\\machine-code-file";

        Assembler.AsmReader in = null;
        try {
            in = new Assembler.AsmReader(inFileString);
        } catch (IOException e) {
            System.out.println("error in opening " + inFileString);
            System.exit(1);
        }

        TreeMap<Integer, String[]> assembly = new TreeMap<>();

        int numLine = 0;
        String[] fields = new String[5];
        while (Assembler.readAndParse(in, fields)) {
            numLine++;
            assembly.put(numLine, fields.clone());
        }

        // Pass 1: validate
        if (!Assembler.ErrorChecker(assembly)) {
            System.exit(1);
        }

       Assembler.ConvertMachineCode assembly_machine = new Assembler.ConvertMachineCode(assembly);

        // Pass 2: write output
        try (PrintWriter out = new PrintWriter(new FileWriter(outFileString))) {
//            for (String[] f : assembly.values()) {
//                String line = "label=[" + f[LABEL] + "]"
//                        + " opcode=[" + f[OPCODE] + "]"
//                        + " arg0=[" + f[ARG0] + "]"
//                        + " arg1=[" + f[ARG1] + "]"
//                        + " arg2=[" + f[ARG2] + "]";
//                System.out.println(line);
//            }
            for (Integer key : assembly_machine.assembly_machinecode.values()) {
                out.println(key);
            }
        } catch (IOException e) {
            System.out.println("error in opening " + outFileString);
            System.exit(1);
        }
    }
}