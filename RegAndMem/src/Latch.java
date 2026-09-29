public class Latch {
    private boolean output;
    public Latch() {
        this.output = false;
    }
    public void set(boolean data_input, boolean write_en) {
        this.output = ((data_input&&write_en)|| output)&&!(!data_input&&write_en);
    }

    public boolean get() {
        return this.output;
    }
}
