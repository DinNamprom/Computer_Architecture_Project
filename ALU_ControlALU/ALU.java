package ALU_ControlALU;

public class ALU {
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
