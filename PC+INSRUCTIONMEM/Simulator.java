import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Simulator {

    public static void main(String[] args) {
        //สร้าง Object state
        StateType state = new StateType();

        //โหลดคำสั่งเข้าหน่วยความจำ
        try {
            File file = new File("machine_code.txt");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextInt()) {
                state.mem[state.numMemory] = scanner.nextInt();
                state.numMemory++; // ต้องเท่ากับจำนวนบรรทัดใน machine-code file
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error opening file");
            System.exit(1); // Call exit Error
        }

        int opcode;


        //Fetch Instruction, อัปเดต PC และเชื่อมต่อระบบแสดงผล
        while (true) {
            // เรียก 1 ครั้ง ก่อน instruction executes
            printState(state);

            // 1. Fetch: ดึงคำสั่งจากตำแหน่ง PC ปัจจุบัน
            int current_instruction = state.mem[state.pc];

            // 2. Decode: แยก opcode ออกมา (bits 24-22)
            opcode = (current_instruction >> 22) & 0x7;

            // 3. PC Update: ขยับ PC ไปชี้บรรทัดถัดไปเสมอ
            state.pc = state.pc + 1;

            // 4. Execute: ตรวจสอบและรันคำสั่ง
            if (opcode == 6) {
                // คำสั่ง halt (opcode 110)
                break; // ออกจาก loop เพื่อเตรียมจบโปรแกรม
            }
            else if (opcode == 7) {
                // คำสั่ง noop (opcode 111) ไม่ทำอะไรเลย
                continue;
            }
            // ... (เพื่อนที่จะทำ ALU จะมาเขียนคำสั่ง add, nand, lw, sw ต่อตรงนี้) ...
        }

        // เรียกอีก 1 ครั้ง ก่อน simulator exits
        printState(state);
        System.exit(0); // Call exit(0) ถ้าทำเสร็จโดยไม่มี errors
    }

    // พิมพ์สถานะของระบบ
    public static void printState(StateType state) {
        // หมายเหตุ: ห้ามแก้ไข printState หรือ stateStruct ของเทมเพลตอาจารย์
        // ใส่ลอจิกการ print memory, register, pc ตาม format ที่กำหนด
    }
}