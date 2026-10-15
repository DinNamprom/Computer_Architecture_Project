package PCAndINSRUCTIONMEM;

import ALU_ControlALU.ALU;
import ALU_ControlALU.ExecuteMemoryStage;
import ImmGenAndControlUnit.ControlUnit;
import RegAndMem.DataMem;

import java.io.IOException;
import java.nio.file.Path;

import static ImmGenAndControlUnit.ImmGen.immGen;

public class Simulator {

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java Simulator [machine-code file]");
            System.exit(1);
            return;
        }

        try {
            // รับ path จาก args หรือใช้ชื่อไฟล์เดิม
            Path path = Path.of(args.length == 1 ? args[0] : "machine_code/machine_code_file.txt");
            StateType state = new StateType();
            MachineCodeLoader.load(path, state);
            int instructionCount = simulate(state);

            System.out.println("machine halted");
            System.out.printf("total of %d instructions executed\n", instructionCount);
            System.out.println("final state of machine:");
            printState(state);
        } catch (IOException e) {
            System.err.println("Error reading machine-code file: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public static int simulate(StateType state) {
        int instructionCount = 0;
        RegisterBridge registers = new RegisterBridge(state);
        DataMem dataMem = new DataMem();
        // โหลดโปรแกรมด้วย byte address; ฝั่ง execute ต้องใช้ word address ให้ตรงกัน
        for (int i = 0; i < state.numMemory; i++) {
            dataMem.store(i * 4, state.mem[i], true);
        }
        ExecuteMemoryStage eMemStage = new ExecuteMemoryStage(registers.getRegister(), dataMem);

        while (true) {
            int currentPC = state.pc;
            if (currentPC < 0 || currentPC >= StateType.NUMMEMORY) {
                throw new IllegalStateException("PC out of range: " + currentPC);
            }
            int opcode = state.mem[currentPC] >> 22 & 0x7;
            int rs1Addr = state.mem[currentPC] >> 19 & 0x7;
            int rs2Addr = state.mem[currentPC] >> 16 & 0x7;
            int destReg = state.mem[currentPC] & 0x7;
            int immediate = immGen(state.mem[currentPC] & 0xFFFF);
            // แสดงสถานะก่อนรันแต่ละคำสั่ง
            registers.readInto(state);
            printState(state);
            boolean halted = ControlUnit.getHalt(opcode) == 1;
            int nextPC = currentPC + 1;
            if (ControlUnit.getJalr(opcode) == 1) {
                // เก็บ PC+1 ใน regB; ถ้า A กับ B เป็นตัวเดียวกันให้ไป PC+1
                int target = rs1Addr == rs2Addr ? nextPC : state.reg[rs1Addr];
                if (rs2Addr != 0) registers.getRegister().set(rs2Addr, nextPC);
                nextPC = target;
            } else if (ControlUnit.getBranch(opcode) == 1) {
                // เปรียบเทียบค่าที่เก็บใน register ไม่ใช่เลขหมาย register
                if (ALU.compute("beq", state.reg[rs1Addr], state.reg[rs2Addr]) == 0) {
                    nextPC += immediate;
                }
            } else if (!halted && opcode != 7) {
                eMemStage.execute(opcode, rs1Addr, rs2Addr, destReg, immediate);
            }
            state.pc = nextPC;
            // แสดงผลล่าสุดได้ทันที แม้คำสั่งนี้จะจบโปรแกรม
            registers.readInto(state);
            instructionCount++; // นับ noop และ halt ด้วย
            if (halted) {
                return instructionCount;
            }
        }
    }

    // พิมพ์สถานะของระบบ
    public static void printState(StateType state) {

        System.out.print("\n@@@\nstate:\n");
        System.out.printf("\tpc %d\n", state.pc);
        System.out.print("\tmemory:\n");

        // แสดงเฉพาะ memory ที่โหลดมา
        for (int i = 0; i < state.numMemory; i++) {
            System.out.printf("\t\tmem[ %d ] %d\n", i, state.mem[i]);
        }

        System.out.print("\tregisters:\n");
        // แสดง register ทั้ง 8 ตัว
        for (int i = 0; i < StateType.NUMREGS; i++) {
            System.out.printf("\t\treg[ %d ] %d\n", i, state.reg[i]);
        }
        System.out.print("end state\n");
    }
}
