package ImmGenAndControlUnit;

public class ControlUnit {

    public static int decodeInstruction(int instruction) {
        //(Opcode 3 บิต อยู่ที่บิต 24-22)
        int opcode = (instruction >> 22) & 0x7;
        return opcode;
    }

    // 1. RegWrite: เป็น 1 เมื่อเป็นคำสั่ง add (0), nand (1), lw (2), jalr (5)
    public static int getRegWrite(int opcode) {
        if (opcode == 0b000 || opcode == 0b001 || opcode == 0b010 || opcode == 0b101) {
            return 1;
        }
        return 0;
    }

    // 2. RegDst: เป็น 1 (rt) เมื่อเป็นคำสั่ง lw (2), jalr (5) | นอกนั้น (0 หรือ X) คืนค่า 0
    public static int getRegDst(int opcode) {
        if (opcode == 0b010 || opcode == 0b101) {
            return 1;
        }
        return 0;
    }

    // 3. ALUSrc: เป็น 1 (imm) เมื่อเป็นคำสั่ง lw (2), sw (3) | นอกนั้น (0 หรือ X) คืนค่า 0
    public static int getALUSrc(int opcode) {
        if (opcode == 0b010 || opcode == 0b011) {
            return 1;
        }
        return 0;
    }

    // 4. ALUOp: คืนค่าเป็น String ตามชนิดของการคำนวณ
    public static String getALUOp(int opcode) {
        switch (opcode) {
            case 0b000: // add
            case 0b010: // lw
            case 0b011: // sw
                return "add";
            case 0b001: // nand
                return "nand";
            case 0b100: // beq
                return "beq";
            default:    // jalr, halt, noop
                return "NONE";
        }
    }

    // 5. MemRead: เป็น 1 เฉพาะคำสั่ง lw (2)
    public static int getMemRead(int opcode) {
        if (opcode == 0b010) {
            return 1;
        }
        return 0;
    }

    // 6. MemWrite: เป็น 1 เฉพาะคำสั่ง sw (3)
    public static int getMemWrite(int opcode) {
        if (opcode == 0b011) {
            return 1;
        }
        return 0;
    }

    // 7. MemToReg: lw (2) คืนค่า 1 (Mem), jalr (5) คืนค่า 2 (PC+1) | นอกนั้นคืนค่า 0 (ALU หรือ X)
    public static int getMemToReg(int opcode) {
        if (opcode == 0b010) {
            return 1;
        } else if (opcode == 0b101) {
            return 2;
        }
        return 0;
    }

    // 8. Branch: เป็น 1 เฉพาะคำสั่ง beq (4)
    public static int getBranch(int opcode) {
        if (opcode == 0b100) {
            return 1;
        }
        return 0;
    }

    // 9. Jalr: เป็น 1 เฉพาะคำสั่ง jalr (5)
    public static int getJalr(int opcode) {
        if (opcode == 0b101) {
            return 1;
        }
        return 0;
    }

    // 10. Halt: เป็น 1 เฉพาะคำสั่ง halt (6)
    public static int getHalt(int opcode) {
        if (opcode == 0b110) {
            return 1;
        }
        return 0;
    }
}