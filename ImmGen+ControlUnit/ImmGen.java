public class ImmGen {

    // ดึง offsetField (bit 15-0) ออกจาก instruction 32-bit
    static int getOffsetField(int instruction) {
        return instruction & 0xFFFF;
    }

    // sign extension: 16-bit 2's complement -> 32-bit int
    static int convertNum(int num) {
        if ((num & (1 << 15)) != 0) { //เลขที่รับมา & กับ int ที่ตำแหน่งที่ 1 LS 15 ครั้งหรือก็คือเทียบ bit ที่ 15 กับ num(0000 0000 0000 0000 1000 0000 0000 0000) เพื่อเช็คว่าเป็น 2's Com ไหม
            num |= 0xFFFF0000; //OR ด้วย 11111111111111110000000000000000 เพื่อให้ได้ค่า 2's Com แบบครบ 32bits
        }
        return num;
    }

    // IMM Generator
    static int immGen(int instruction) {
        return convertNum(getOffsetField(instruction));
    }

    public static void main(String[] args) {
        // Test Case
        System.out.println(immGen(8454151));   // lw 0 1 five     -> 7 can use 0x810007 also
        System.out.println(immGen(0x8a0003));   // lw 1 2 3        -> 3
        System.out.println(immGen(0x1010002));  // beq 0 1 2       -> 2
        System.out.println(immGen(0x100fffd));  // beq 0 0 start   -> -3

        // Corner Case
        System.out.println(convertNum(0x7FFF)); // 32767
        System.out.println(convertNum(0x8000)); // -32768
        System.out.println(convertNum(0));      // 0
    }
}