package ALU_ControlALU;

import ImmGenAndControlUnit.ControlUnit;
import RegAndMem.DataMem;
import RegAndMem.Reg;


public class ExecuteMemoryStage{

    private final Reg reg;
    private final DataMem dataMem;

    public ExecuteMemoryStage(Reg reg, DataMem dataMem){
        this.reg = reg;
        this.dataMem = dataMem;
    }

    public Reg getReg() {return reg;}
    public DataMem getdataMem() {return dataMem;}

    /**
        use for case bit MSB(bit 32) < 0
        during using math.pow it sudden cut off 1
        that's why we need to add it back one for fixes bug
     */
    public int signed(int rawValue){
        if( rawValue < 0 ) return rawValue;
        else return rawValue;
    }

    public ExecuteResult execute(int opcode, int rs1Addr, int rs2Addr, int destReg, int immediate) {

        /**
            Read Register File
         */
        int rawRs1 = reg.get(rs1Addr);     // ค่าจาก register ตัวที่ 1
        int rawRs2 = reg.get(rs2Addr);     // ค่าจาก register ตัวที่ 2 (ใช้ตอน store หรือ R-type)
        int rs1 = signed(rawRs1);
        int rs2 = signed(rawRs2);

        /**
            Call ControlUnit used
         */
        int aluSrc = ControlUnit.getALUSrc(opcode);
        int regWrite = ControlUnit.getRegWrite(opcode);
        String signal = ControlUnit.getALUOp(opcode);       // opcode -> signal     EX. 000 -> and
        int regDst = ControlUnit.getRegDst(opcode);         // used this to writeBackAddress instead when opcode == 2
        int memRead = ControlUnit.getMemRead(opcode);
        int memWrite = ControlUnit.getMemWrite(opcode);
        int memToReg = ControlUnit.getMemToReg(opcode);


        /**
            ALUSrc MUX - Deciding Which op2 to be used
         */
        int op2;

        if(aluSrc == 1){                // lw, sw
            op2 = immediate;
        } else if (aluSrc == 0) {       // add, nand, beq
            op2 = rs2;
        }else{                          // jalr, halt, noop
            op2 = 0;
        }

        /**
            ALU
         */
        int aluResult =  0;
        if(!signal.equals("NONE")) aluResult = ALU.compute(signal,rs1,op2);     // if not jalr.. calculate it

        /**
            Memory Access ----> lw / sw
         */
        int memData = 0;
        if(memRead == 1){
            int rawMemData = dataMem.load(aluResult*4);
            memData = signed(dataMem.load(aluResult*4));     // lw -- load data from aluResult address to store in memData
        }
        if(memWrite == 1)dataMem.store(aluResult*4, rs2, true);       // sw -- get value in rs2 store in the aluResult address

        // instead of checking from opcode use memWrite and memRead
//        if(opcode == 2){
//            int rawMemData = dataMem.load(aluResult);          // lw -- load data from aluResult address to store in memData
//            memData = signed(rawMemData);
//        }
//        if(opcode == 3) {
//            dataMem.store(aluResult, rs2, true);    // sw -- get value in rs2 store in the aluResult address
//        }

        /**
            Write back MUX -- write the result back to Register
         */
        int writeBackValue = 0;
        int writeBackAddress = destReg;         // default

            /*
                Decide to write back to Register or not
                if 1 => has to write back
                if 0 => sw send to memory not register, beq only compare
            */
        if(regWrite == 1){
            if(memToReg == 1){
                writeBackValue = memData;
                writeBackAddress = rs2Addr;
            }
            else {
                writeBackValue = aluResult;     // if not lw -- write aluResult in
            }
//            else{
//                writeBackValue = memData;                   // if opcode == lw --  write memData back
//                writeBackAddress = rs2Addr;                 // then write into rs2Addr
//            }

            reg.set(writeBackAddress,writeBackValue);
        }

        return new ExecuteResult(aluResult,writeBackAddress);
    }

//    public int decideALUSrc(int opcode){
//        switch (opcode){
//            case 2: return 1;   // lw
//            case 3: return 1;   // sw
//            case 0: return 0;   // add
//            case 1: return 0;   // nand
//            case 4: return 0;   // beq
//            default: return -1; //jalr, halt, noop
//        }
//    }
//
//    public int decideRedWrite(int opcode){
//        switch (opcode){
//            case 0: return 1;   // add
//            case 1: return 1;   // nand
//            case 2: return 1;   // lw
//            case 5: return 1;   // jalr
//            default: return 0;  //sw ,beq ,halt ,noop
//        }
//    }

    public int getRegisterFixed(int address){
        int rawAddr = reg.get(address);
        return signed(rawAddr);
    }

}