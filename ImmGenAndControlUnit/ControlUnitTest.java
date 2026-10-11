package ImmGenAndControlUnit;

/**
 * Test harness สำหรับ controlUnit (ไม่ต้องใช้ JUnit)
 * สมมติว่า:
 *  - StateType มี field: pc, mem[], reg[], numMemory และมี constructor ว่าง
 *  - controlUnit(StateType) เป็น static อยู่ในคลาส ControlUnit (package เดียวกัน)
 * ถ้าชื่อคลาส/เมธอดต่างจากนี้ ให้แก้ที่บรรทัดเรียก ControlUnit.controlUnit(...)
 */
public class ControlUnitTest {

    static final int ADD = 0, NAND = 1, LW = 2, SW = 3, BEQ = 4, JALR = 5, HALT = 6, NOOP = 7;

    static int passed = 0, failed = 0;

    // ---------- ตัวช่วยสร้าง machine code ----------
    static int encR(int op, int a, int b, int rd) { return (op << 22) | (a << 19) | (b << 16) | rd; }
    static int encI(int op, int a, int b, int off) { return (op << 22) | (a << 19) | (b << 16) | (off & 0xFFFF); }
    static int encJ(int op, int a, int b)           { return (op << 22) | (a << 19) | (b << 16); }
    static int encO(int op)                         { return op << 22; }

    // ---------- ตัวช่วยเช็คผล ----------
    static void check(String name, int expected, int actual) {
        if (expected == actual) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name + "  expected=" + expected + " actual=" + actual);
        }
    }

    static StateType newState(int pc, int instruction) {
        StateType s = new StateType();
        s.pc = pc;
        s.mem[pc] = instruction;
        return s;
    }

    public static void main(String[] args) {
        testEncoding();
        testAdd();
        testNand();
        testLw();
        testSw();
        testBeq();
        testJalr();
        testZeroRegister();
        testHaltNoop();
        testCountProgram();

        System.out.println("\npassed = " + passed + ", failed = " + failed);
        if (failed > 0) System.exit(1);
    }

    // ตรวจ encoder กับตัวอย่างในเอกสารก่อน (ถ้าอันนี้ผิด test อื่นไม่มีความหมาย)
    static void testEncoding() {
        check("enc lw 0 1 7",     0x810007,  encI(LW, 0, 1, 7));
        check("enc lw 1 2 3",     0x8a0003,  encI(LW, 1, 2, 3));
        check("enc add 1 2 1",    0xa0001,   encR(ADD, 1, 2, 1));
        check("enc beq 0 1 2",    0x1010002, encI(BEQ, 0, 1, 2));
        check("enc beq 0 0 -3",   0x100fffd, encI(BEQ, 0, 0, -3));
        check("enc halt",         0x1800000, encO(HALT));
        check("enc noop",         0x1c00000, encO(NOOP));
    }

    static void testAdd() {
        StateType s = newState(0, encR(ADD, 1, 2, 3));
        s.reg[1] = 5; s.reg[2] = 7;
        boolean h = ControlUnit.controlUnit(s);
        check("add result", 12, s.reg[3]);
        check("add pc", 1, s.pc);
        check("add not halt", 0, h ? 1 : 0);
    }

    static void testNand() {
        StateType s = newState(0, encR(NAND, 1, 2, 3));
        s.reg[1] = 6; s.reg[2] = 3;                 // ~(6 & 3) = ~2 = -3
        ControlUnit.controlUnit(s);
        check("nand result", -3, s.reg[3]);
        check("nand pc", 1, s.pc);
    }

    static void testLw() {
        // offset บวก: mem[10 + 2] -> reg1
        StateType s = newState(0, encI(LW, 2, 1, 2));
        s.reg[2] = 10; s.mem[12] = 123;
        ControlUnit.controlUnit(s);
        check("lw positive offset", 123, s.reg[1]);
        check("lw pc", 1, s.pc);

        // offset ลบ: mem[10 - 3] -> reg1 (ทดสอบ sign extension)
        s = newState(0, encI(LW, 2, 1, -3));
        s.reg[2] = 10; s.mem[7] = 456;
        ControlUnit.controlUnit(s);
        check("lw negative offset", 456, s.reg[1]);

        // ค่าขอบ -32768
        s = newState(0, encI(LW, 2, 1, -32768));
        s.reg[2] = 40000; s.mem[40000 - 32768] = 789;
        ControlUnit.controlUnit(s);
        check("lw offset -32768", 789, s.reg[1]);

        // ค่าขอบ +32767
        s = newState(0, encI(LW, 0, 1, 32767));
        s.mem[32767] = 55;
        ControlUnit.controlUnit(s);
        check("lw offset 32767", 55, s.reg[1]);
    }

    static void testSw() {
        StateType s = newState(0, encI(SW, 2, 1, 4));
        s.reg[1] = 999; s.reg[2] = 20;
        ControlUnit.controlUnit(s);
        check("sw positive offset", 999, s.mem[24]);
        check("sw pc", 1, s.pc);

        s = newState(0, encI(SW, 2, 1, -5));
        s.reg[1] = 777; s.reg[2] = 20;
        ControlUnit.controlUnit(s);
        check("sw negative offset", 777, s.mem[15]);
    }

    static void testBeq() {
        // เท่ากัน -> pc + 1 + offset
        StateType s = newState(4, encI(BEQ, 1, 2, 3));
        s.reg[1] = 5; s.reg[2] = 5;
        ControlUnit.controlUnit(s);
        check("beq taken", 8, s.pc);

        // ไม่เท่ากัน -> pc + 1
        s = newState(4, encI(BEQ, 1, 2, 3));
        s.reg[1] = 5; s.reg[2] = 6;
        ControlUnit.controlUnit(s);
        check("beq not taken", 5, s.pc);

        // offset ลบ (ตัวอย่างในเอกสาร: address 4, beq 0 0 -3 -> pc = 2)
        s = newState(4, encI(BEQ, 0, 0, -3));
        ControlUnit.controlUnit(s);
        check("beq negative offset", 2, s.pc);
    }

    static void testJalr() {
        // ปกติ: regB = pc+1, pc = regA
        StateType s = newState(10, encJ(JALR, 2, 4));
        s.reg[2] = 50;
        ControlUnit.controlUnit(s);
        check("jalr normal reg4", 11, s.reg[4]);
        check("jalr normal pc", 50, s.pc);

        // regA == regB: เก็บ pc+1 แล้วกระโดดไป pc+1
        s = newState(10, encJ(JALR, 3, 3));
        s.reg[3] = 99;
        ControlUnit.controlUnit(s);
        check("jalr same reg value", 11, s.reg[3]);
        check("jalr same reg pc", 11, s.pc);
    }

    static void testZeroRegister() {
        StateType s = newState(0, encR(ADD, 1, 1, 0));   // add 1 1 0 พยายามเขียนลง $0
        s.reg[1] = 5;
        ControlUnit.controlUnit(s);
        check("$0 stays 0", 0, s.reg[0]);
    }

    static void testHaltNoop() {
        StateType s = newState(6, encO(HALT));
        boolean h = ControlUnit.controlUnit(s);
        check("halt returns true", 1, h ? 1 : 0);
        check("halt pc+1", 7, s.pc);

        s = newState(5, encO(NOOP));
        s.reg[1] = 42;
        h = ControlUnit.controlUnit(s);
        check("noop returns false", 0, h ? 1 : 0);
        check("noop pc+1", 6, s.pc);
        check("noop no change", 42, s.reg[1]);
    }

    // โปรแกรมนับ 5 ถึง 0 จากเอกสาร
    static void testCountProgram() {
        int[] code = { 8454151, 9043971, 655361, 16842754, 16842749,
                29360128, 25165824, 5, -1, 2 };
        StateType s = new StateType();
        s.numMemory = code.length;
        for (int i = 0; i < code.length; i++) s.mem[i] = code[i];
        s.pc = 0;

        int count = 0;
        boolean halted = false;
        while (!halted && count < 1000) {          // กัน infinite loop ตอนทดสอบ
            halted = ControlUnit.controlUnit(s);
            count++;
        }
        check("count program: halted", 1, halted ? 1 : 0);
        check("count program: instructions", 17, count);
        check("count program: reg1", 0, s.reg[1]);
        check("count program: reg2", -1, s.reg[2]);
        check("count program: final pc", 7, s.pc);
        check("count program: mem[7] unchanged", 5, s.mem[7]);
        check("count program: mem[8] unchanged", -1, s.mem[8]);
    }
}