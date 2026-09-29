import RegAndMem.DataMem;
import RegAndMem.Reg;

public class Main {
    public static void main(String[] args) {
        DataMem memory = new DataMem();

        // 1. Create an 8-bit address for decimal 14 (Binary: 00001110)
        // We use 14 specifically to test the wrap-around logic.
        // Byte 1 goes to Row 0, Col 14
        // Byte 2 goes to Row 0, Col 15
        // Byte 3 wraps to Row 1, Col 0
        // Byte 4 goes to Row 1, Col 1
        boolean[] address14 = {false, false, false, false, true, true, true, false};

        // 2. Create 32-bit data to store (4 distinct byte patterns)
        // Byte 0: 11110000 | Byte 1: 10101010 | Byte 2: 00001111 | Byte 3: 01010101
        boolean[] dataToStore = {
                true, true, true, true, false, false, false, false,  // Byte 0
                true, false, true, false, true, false, true, false,  // Byte 1
                false, false, false, false, true, true, true, true,  // Byte 2
                false, true, false, true, false, true, false, true   // Byte 3
        };

        // Print original data
        System.out.println("Original 32-bit Data:");
        printBinary(dataToStore);

        // 3. Store the 32-bit word
        System.out.println("\nStoring data at Address 14 (testing wrap-around)...");
        memory.store(address14, dataToStore, true);

        // 4. Load the 32-bit word back
        System.out.println("Loading data from Address 14...");
        boolean[] loadedData = memory.load(address14);

        // 5. Print retrieved data to verify a perfect match
        System.out.println("\nRetrieved 32-bit Data:");
        printBinary(loadedData);

        // Check for success
        boolean success = true;
        for (int i = 0; i < 32; i++) {
            if (dataToStore[i] != loadedData[i]) success = false;
        }
        System.out.println("\nTest Passed: " + success);

        Reg registerFile = new Reg();

        // --- TEST ADDRESSES ---
        // Address 0 (x0) - Should be protected
        boolean[] addrX0 = {false, false, false};

        // Address 2 (x2) - Valid register
        boolean[] addrX2 = {false, true, false};

        // Address 7 (x7) - Out of bounds (since size is 7, max index is 6)
        boolean[] addrX8 = {true, true, true};

        // --- TEST DATA ---
        // 8-bit data: 10101111 (Decimal 175)
        boolean[] testData = {true, false, true, false, true, true, true, true};

        System.out.println("--- TEST 1: The x0 Hardware Protection ---");
        System.out.println("Attempting to write to x0...");
        registerFile.set(addrX0, testData);

        System.out.println("\n--- TEST 2: Padding and Storing Data ---");
        System.out.println("Writing 8-bit data (10101111) into register x2...");
        registerFile.set(addrX2, testData);
        System.out.println("Write complete.");

        System.out.println("\n--- TEST 3: Reading Data Back ---");
        System.out.println("Reading 32-bit data from register x2:");
        boolean[] x2Data = registerFile.get(addrX2);
        printBinary(x2Data);
        System.out.println("(Notice how your logic perfectly padded the upper 24 bits with zeros!)");

        System.out.println("\n--- TEST 4: Out of Bounds Protection ---");
        System.out.println("Attempting to write to address 8...");
        registerFile.set(addrX8, testData);
    }

    // Helper method to print boolean arrays nicely
    private static void printBinary(boolean[] data) {
        for (int i = 0; i < data.length; i++) {
            if (i > 0 && i % 8 == 0) System.out.print(" | "); // Split by bytes
            System.out.print(data[i] ? "1" : "0");
        }
        System.out.println();
    }
}