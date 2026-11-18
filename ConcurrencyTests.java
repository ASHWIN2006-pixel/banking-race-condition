package bankingrace;

import java.nio.file.Path;

/** Plain-Java test entry point; no JUnit or external dependencies required. */
public final class ConcurrencyTests {
    public static void main(String[] args) throws InterruptedException {
        ConcurrencyTester.runAll(Path.of("logs", "test-transactions.log"));
    }
}
