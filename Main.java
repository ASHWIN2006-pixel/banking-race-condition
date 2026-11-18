package bankingrace;

import java.nio.file.Path;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws InterruptedException {
        Path log = Path.of("logs", "transactions.log");
        System.out.println("Banking Race Condition Investigation");
        ConcurrencyTester.runAll(log);
        System.out.println("Transaction log: " + log.toAbsolutePath());
    }
}
