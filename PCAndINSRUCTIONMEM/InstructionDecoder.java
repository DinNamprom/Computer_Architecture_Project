public final class InstructionDecoder {
    public static final int HALT = 6;
    public static final int NOOP = 7;

    private InstructionDecoder() {
    }

    public static int getOpcode(int instruction) {
        return (instruction >>> 22) & 0x7;
    }

    public static int getRegA(int instruction) {
        return (instruction >>> 19) & 0x7;
    }

    public static int getRegB(int instruction) {
        return (instruction >>> 16) & 0x7;
    }

    public static int getDestReg(int instruction) {
        return instruction & 0x7;
    }

    // offset 0 ถึง 65535
    public static int getOffsetField(int instruction) {
        return instruction & 0xFFFF;
    }

    // แปลง offset 16 บิตให้มีเครื่องหมาย
    public static int getSignedOffset(int instruction) {
        return (short) getOffsetField(instruction);
    }
}
