package RegAndMem;

public class DataMem {
    private OneByteMem[][] mem;
    private final int size = 512;
    public DataMem() {
        mem = new OneByteMem[size][size];
        for (int i = 0;i < size;i++) {
            for (int j = 0;j < size;j++) {
                mem[i][j] = new OneByteMem();
            }
        }
    }

    public void store(int address, int data, boolean en) {
        int row = address / size;
        int col = address % size;
        boolean[] b_data = decimal2binary(data);
        for (int i = 0;i < 4;i++) {
            boolean[] c_data = new boolean[8];
            System.arraycopy(b_data, (i * 8), c_data, 0, 8);
            if (col >= size) {
                row++;
                col = 0;
            }
            if (row >= size) {
                System.out.println("Hardware Fault: Memory Overflow");
                break;
            }
            mem[row][col].set(c_data,en);
            col++;
        }
    }

    public int load(int address) {
        int row = address / size;
        int col = address % size;
        boolean[] data = new boolean[32];
        for (int i = 0; i < 4; i++) {
            if (col >= size) {
                row++;
                col = 0;
            }
            if (row >= size) {
                System.out.println("Hardware Fault: Memory Overflow");
                break;
            }
            OneByteMem m = mem[row][col];
            Latch[] latches = m.get();
            for (int j = 0; j < 8; j++) data[(i * 8) + j] = latches[j].get();

            col++;
        }
        return binary2decimal(data);
    }
    private int binary2decimal(boolean[] data) {
        int value = 0;
        for (boolean bit : data) {
            value = (value << 1) | (bit ? 1 : 0);
        }
        return value;
    }

    private boolean[] decimal2binary(int decimal) {
        boolean[] binary = new boolean[32];

        for (int i = 0; i < 32; i++) {
            int mask = 1 << (32 - 1 - i);
            binary[i] = (decimal & mask) != 0;
        }

        return binary;
    }
}
