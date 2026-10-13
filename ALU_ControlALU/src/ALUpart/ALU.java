package ALUpart;

public class ALU {
    public static String getALUSignal (int opcode){
        switch (opcode){
            case 0: return "add";
            case 1: return "nand";
            case 2: return "add"; //lw
            case 3: return "add"; //sw
            case 4: return "beq"; // if subtract result == 0 means equal
            case 5: return "NONE"; // jalr
            case 6: return "NONE"; // halt
            case 7: return "NONE"; // noop
            default: return "NONE";
        }
    }

    public static int compute (String signal, int rs1, int rs2){
        if(signal.equals("add")){
            return rs1 + rs2;
        }else if(signal.equals("nand")){
            return ~(rs1 & rs2);
        }else if(signal.equals("beq")){
            return rs1 - rs2;
        }else{
            return 0;
        }
    }
}
