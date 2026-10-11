package ImmGenAndControlUnit;


import static ImmGenAndControlUnit.ImmGen.convertNum;
import static ImmGenAndControlUnit.StateType.NUMMEMORY;


public class ControlUnit
{
    static void checkAddr(int addr) {
        if (addr < 0 || addr >= NUMMEMORY) {
            System.out.println("error: address out of range: " + addr);
            System.exit(1);
        }
    }

    // Control Unit: decode + execute 1 instruction
// คืนค่า true ถ้าเป็น halt (ให้ผู้เรียกหยุด loop)
    static boolean controlUnit(StateType s) {
        checkAddr(s.pc);
        int instruction    = s.mem[s.pc];
        int opcode  = (instruction >> 22) & 0x7;   // bits 24-22
        int regA    = (instruction >> 19) & 0x7;   // bits 21-19
        int regB    = (instruction >> 16) & 0x7;   // bits 18-16
        int destReg = instruction & 0x7;           // bits 2-0 (เฉพาะ R-type)



        switch (opcode) { //binary compare
            case 0:  // add
            case 1:  // nand
                s.reg[destReg] = ALU.compute(ALU.getALUSignal(opcode), s.reg[regA], s.reg[regB]);
                s.pc++;
                break;
            case 2: { // lw
                int addr = ALU.compute(ALU.getALUSignal(opcode), s.reg[regA], convertNum(instruction & 0xFFFF));
                checkAddr(addr);
                s.reg[regB] = s.mem[addr];
                s.pc++;
                break;
            }
            case 3: { // sw
                int addr = ALU.compute(ALU.getALUSignal(opcode), s.reg[regA], convertNum(instruction & 0xFFFF));
                checkAddr(addr);
                s.mem[addr] = s.reg[regB];
                s.pc++;
                break;
            }
            case 4: { // beq
                int diff = ALU.compute(ALU.getALUSignal(opcode), s.reg[regA], s.reg[regB]);
                if (diff == 0) s.pc = s.pc + 1 + convertNum(instruction & 0xFFFF);
                else s.pc++;
                break;
            }
            case 5: { // jalr
                int next = s.pc + 1;
                int target = (regA == regB) ? next : s.reg[regA];
                s.reg[regB] = next;
                s.pc = target;
                break;
            }
            case 6:  // halt (O-type)
                s.pc++;
                return true;
            case 7:  // noop (O-type)
                s.pc++;
                break;
            default:
                System.out.println("error: unknown opcode " + opcode);
                System.exit(1);
        }
        s.reg[0] = 0;                       // $0 เป็น 0 เสมอ
        return false;
    }
}

//static void run(StateType s) {
//    int count = 0;
//    boolean halted = false;
//    while (!halted) {
//        printState(s);                  // ก่อน execute ทุก instruction
//        halted = controlUnit(s);
//        count++;
//    }
//    System.out.println("machine halted");
//    System.out.println("total of " + count + " instructions executed");
//    System.out.println("final state of machine:");
//    printState(s);                      // ก่อน exit
//}