public class ControlUnit
{
    static void checkAddr(int addr) {
        if (addr < 0 || addr >= StateType.NUMMEMORY) {
            throw new IllegalStateException("Address out of range: " + addr);
        }
    }

    // Control Unit: decode + execute 1 instruction
// คืนค่า true ถ้าเป็น halt (ให้ผู้เรียกหยุด loop)
    public static boolean controlUnit(StateType s) {
        checkAddr(s.pc);
        int instruction    = s.mem[s.pc];
        int opcode  = (instruction >> 22) & 0x7;   // bits 24-22
        int regA    = (instruction >> 19) & 0x7;   // bits 21-19
        int regB    = (instruction >> 16) & 0x7;   // bits 18-16
        int destReg = instruction & 0x7;           // bits 2-0 (เฉพาะ R-type)



        switch (opcode) { //binary compare
            case 0:  // add (R-type)
                s.reg[destReg] = s.reg[regA] + s.reg[regB];
                s.pc++;
                break;
            case 1:  // nand (R-type)
                s.reg[destReg] = ~(s.reg[regA] & s.reg[regB]);
                s.pc++;
                break;
            case 2:  // lw
                int addrL = s.reg[regA] + ImmGen.convertNum(instruction & 0xFFFF);
                checkAddr(addrL);
                s.reg[regB] = s.mem[addrL];
                s.pc++;
                break;
            case 3:  // sw
                int addrS = s.reg[regA] + ImmGen.convertNum(instruction & 0xFFFF);
                checkAddr(addrS);
                s.mem[addrS] = s.reg[regB];
                s.pc++;
                break;
            case 4:  // beq (I-type)
                if (s.reg[regA] == s.reg[regB])
                    s.pc = s.pc + 1 + ImmGen.convertNum(instruction & 0xFFFF);
                else
                    s.pc++;
                break;
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
                throw new IllegalStateException("Unknown opcode: " + opcode);
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