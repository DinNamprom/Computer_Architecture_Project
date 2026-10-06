package RegAndMem;

public class Reg {
    private OneByteMem[][] data;
    private final int size = 8;
    public Reg() {
        data = new OneByteMem[size][4];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < 4; j++) data[i][j] = new OneByteMem();
        }
    }

    public int get(int address) {
        boolean[] output = new boolean[32];
        if (address >= size) {
            System.out.println("Hardware Fault: Address OutOfRange");
            return -1;
        }
        for (int i = 0; i < 4; i++) {
            Latch[] latches = data[address][i].get();
            for (int j = 0; j < 8; j++) output[i*8 + j] = latches[j].get();
        }
        return binary2decimal(output);
    }

    public void set(int address, int value) {
        boolean[] b_value = decial2binary(value);
        if (address == 0) {
            System.out.println("Reserve x0 for 0 value");
            return;
        }
        if (address >= size) {
            System.out.println("Hardware Fault: Address OutOfRange");
            return;
        }
        boolean[] s_data = new boolean[32];
        for (int i = 0; i < b_value.length; i++) s_data[31 - i] = b_value[b_value.length - i - 1];
        for (int i = 0; i < 4; i++) {
            boolean[] byteChunk = new boolean[8];
            System.arraycopy(s_data, (i * 8), byteChunk, 0, 8);
            data[address][i].set(byteChunk, true);
        }
    }

    private int binary2decimal(boolean[] data) {
        int sum = 0;
        for (int i = 0;i < data.length;i++) {
            if (data[i]) sum += (int) Math.pow(2, data.length-1-i);
        }
        return sum;
    }

    private boolean[] decial2binary(int data) {
        boolean[] output = new boolean[32];
        for (int i = 0; i < 32; i++) {
            int mask = 1 << (32 - 1 - i);
            output[i] = (data & mask) != 0;
        }
        return output;
    }


}
