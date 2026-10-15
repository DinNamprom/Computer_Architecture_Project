package PCAndINSRUCTIONMEM;

public final class BitConverter {
    private BitConverter() {
    }

    // ตัวแรกคือบิต 31 ตัวสุดท้ายคือบิต 0
    public static boolean[] toBits32(int value) {
        boolean[] bits = new boolean[32];
        for (int i = 0; i < bits.length; i++) {
            bits[i] = ((value >>> (31 - i)) & 1) != 0;
        }
        return bits;
    }

    // ประกอบบิตกลับเป็น int รวมค่าลบด้วย
    public static int fromBits32(boolean[] bits) {
        if (bits == null || bits.length != 32) {
            throw new IllegalArgumentException("Expected exactly 32 bits.");
        }

        int value = 0;
        for (boolean bit : bits) {
            value = (value << 1) | (bit ? 1 : 0);
        }
        return value;
    }
}
