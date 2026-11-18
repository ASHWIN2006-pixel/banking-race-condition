package bankingrace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/** Thread-safe console and file transaction log. */
public final class TransactionLogger {
    private final Path file;

    public TransactionLogger(Path file) {
        this.file = file;
    }

    public synchronized void log(String mode, String action, long amount, long before, long after) {
        String line = "%s | thread=%s(id=%d) | mode=%s | %s amount=%d balance=%d->%d"
                .formatted(Instant.now(), Thread.currentThread().getName(),
                        Thread.currentThread().threadId(), mode, action, amount, before, after);
        System.out.println(line);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, line + System.lineSeparator(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not write transaction log: " + e.getMessage());
        }
    }
}
