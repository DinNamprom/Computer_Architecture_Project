public class OneByteMem {
    private Latch[] output;
    public OneByteMem() {
        output = new Latch[8];
        for (int i = 0; i < 8; i++) output[i] = new Latch();
    }

    public void set(boolean[] data, boolean en) {
        for (int i = 0; i < 8; i++) output[i].set(data[i], en);
    }

    public Latch[] get() {
        return output;
    }
}
