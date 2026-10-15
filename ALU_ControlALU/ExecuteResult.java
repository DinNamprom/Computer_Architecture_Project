package ALU_ControlALU;

public class ExecuteResult {
    public final int aluResult;
    public int address;

    public ExecuteResult(int aluResult, int address) {
        this.aluResult = aluResult;
        this.address = address;
    }
}