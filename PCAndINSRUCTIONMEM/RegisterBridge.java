package PCAndINSRUCTIONMEM;

import RegAndMem.Reg;

public final class RegisterBridge {
    private Reg registers = new Reg(BitConverter::fromBits32);

    public RegisterBridge(StateType state) {
        writeFrom(state);
        readInto(state);
    }

    // อ่าน register ของ Din ให้ ControlUnit และ printState ใช้
    public void readInto(StateType state) {
        for (int i = 0; i < StateType.NUMREGS; i++) {
            state.reg[i] = registers.get(i);
        }
    }

    // ส่งผลลัพธ์กลับ register จริง; ไม่เขียน register 0
    public void writeFrom(StateType state) {
        for (int i = 1; i < StateType.NUMREGS; i++) {
            registers.set(i, state.reg[i]);
        }
    }

    public void setRegisters(Reg registers) {
        this.registers = registers;
    }

    public Reg getRegister() {
        return registers;
    }
}
