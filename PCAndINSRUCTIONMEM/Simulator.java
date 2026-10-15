package PCAndINSRUCTIONMEM;

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
            Path path = Path.of(args.length == 1 ? args[0] : "machine_code.txt");
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

        while (true) {
            int currentPC = state.pc;
            if (currentPC < 0 || currentPC >= StateType.NUMMEMORY) {
                throw new IllegalStateException("PC out of range: " + currentPC);
            }
            int opcode = state.mem[instructionCount] >> 22 & 0x7;
            int rs1Addr = state.mem[instructionCount] >> 19 & 0x7;
            int rs2Addr = state.mem[instructionCount] >> 16 & 0x7;
            int destReg = state.mem[instructionCount] & 0x7;
            int immediate = immGen(state.mem[instructionCount] & 0xFFFF);
            // แสดงสถานะก่อนรันแต่ละคำสั่ง
            registers.readInto(state);
            printState(state);
            // ControlUnit รันคำสั่งและอัปเดต PC เอง
            boolean halted = ControlUnit.getHalt(opcode) == 1;
            registers.writeFrom(state);
            ExecuteMemoryStage eMemStage = new ExecuteMemoryStage(registers.getRegister(), dataMem);
            eMemStage.execute(currentPC, rs1Addr, rs2Addr, destReg, immediate);
            dataMem = eMemStage.getdataMem();
            registers.setRegisters(eMemStage.getReg());
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
