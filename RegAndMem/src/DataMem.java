public class DataMem {
    private OneByteMem[][] mem;
    private final int size = 16;
    public DataMem() {
        mem = new OneByteMem[size][size];
        for (int i = 0;i < size;i++) {
            for (int j = 0;j < size;j++) {
                mem[i][j] = new OneByteMem();
            }
        }
    }

    public void store(boolean[] address, boolean[] data, boolean en) {
        int deci_address = binary2decimal(address);
        int row = deci_address / size;
        int col = deci_address % size;
        for (int i = 0;i < 4;i++) {
            boolean[] c_data = new boolean[8];
            System.arraycopy(data, (i * 8), c_data, 0, 8);
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

    public boolean[] load(boolean[] address) {
        int deci_address = binary2decimal(address);
        int row = deci_address / size;
        int col = deci_address % size;
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
        return data;
    }

    public void set(boolean[] address, boolean[] data, boolean en) {
        int deci_address = binary2decimal(address);
        int row = deci_address / size;
        int col = deci_address % size;
        this.mem[row][col].set(data, en);
    }

    public OneByteMem get(boolean[] address) {
        int deci_address = binary2decimal(address);
        int row = deci_address / size;
        int col = deci_address % size;
        return this.mem[row][col];
    }

    private int binary2decimal(boolean[] data) {
        int sum = 0;
        for (int i = 0;i < data.length;i++) {
            if (data[i]) sum += (int) Math.pow(2, data.length-1-i);
        }
        return sum;
    }
}
