package PCAndINSRUCTIONMEM;

public class StateType {
    public static final int NUMMEMORY = 65536;
    public static final int NUMREGS = 8;

    public int pc;
    public int[] mem = new int[NUMMEMORY];
    public int[] reg = new int[NUMREGS];
    public int numMemory;

    // Constructor Initialize ค่าเริ่มต้น
    public StateType() {
        this.pc = 0;
        this.numMemory = 0;
        for (int i = 0; i < NUMREGS; i++) {
            this.reg[i] = 0;
        }
    }
}