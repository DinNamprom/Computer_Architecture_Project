import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MachineCodeLoader {
    private MachineCodeLoader() {
    }

    // หนึ่งบรรทัดคือหนึ่ง word รวมข้อมูลจาก .fill
    public static void load(Path path, StateType state) throws IOException {
        state.numMemory = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int lineNumber = state.numMemory + 1;
                if (state.numMemory >= StateType.NUMMEMORY) {
                    throw new IllegalArgumentException("Too many words: maximum is 65536.");
                }

                int value;
                try {
                    value = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Line " + lineNumber + ": expected one signed 32-bit decimal integer.");
                }
                state.mem[state.numMemory++] = value;
            }
        }

        if (state.numMemory == 0) {
            throw new IllegalArgumentException("Machine-code file is empty.");
        }
    }
}
