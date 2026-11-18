package bankingrace;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** Dependency-free deterministic checks for the three implementations. */
public final class ConcurrencyTester {
    private ConcurrencyTester() {}

    public static void runAll(Path logFile) throws InterruptedException {
        System.out.println("\n=== Deterministic concurrency checks ===");
        testUnsafeLostUpdate(logFile);
        testSynchronized(logFile);
        testSemaphore(logFile);
        System.out.println("All concurrency checks passed.");
    }

    private static void testUnsafeLostUpdate(Path logFile) throws InterruptedException {
        var account = new BankAccount(0, new TransactionLogger(logFile));
        CountDownLatch read = new CountDownLatch(2);
        CountDownLatch write = new CountDownLatch(1);
        Runnable tx = () -> account.unsafeChange(100, "DEPOSIT", () -> {
            read.countDown();
            try { read.await(); write.await(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
        });
        Thread a = new Thread(tx, "unsafe-A"), b = new Thread(tx, "unsafe-B");
        a.start(); b.start();
        read.await(); write.countDown();
        a.join(); b.join();
        require(account.balance() == 100, "Controlled unsafe race should lose one of two deposits");
        System.out.println("PASS unsafe: two $100 deposits deterministically expose lost update (balance=$100)");
    }

    private static void testSynchronized(Path logFile) throws InterruptedException {
        var account = new BankAccount(10_000, new TransactionLogger(logFile));
        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            final int id = t;
            workers.add(new Thread(() -> {
                for (int i = 0; i < 500; i++) account.synchronizedChange(1, "DEPOSIT");
                for (int i = 0; i < 500; i++) account.synchronizedChange(1, "WITHDRAW");
            }, "mutex-" + id));
        }
        workers.forEach(Thread::start);
        for (Thread worker : workers) worker.join();
        require(account.balance() == 10_000, "Synchronized expected balance 10000, got " + account.balance());
        System.out.println("PASS synchronized mutex: expected balance=$10000, actual=$" + account.balance());
    }

    private static void testSemaphore(Path logFile) throws InterruptedException {
        var account = new BankAccount(10_000, new TransactionLogger(logFile));
        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            final int id = t;
            workers.add(new Thread(() -> {
                try {
                    for (int i = 0; i < 500; i++) account.semaphoreChange(1, "DEPOSIT");
                    for (int i = 0; i < 500; i++) account.semaphoreChange(1, "WITHDRAW");
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            }, "semaphore-" + id));
        }
        workers.forEach(Thread::start);
        for (Thread worker : workers) worker.join();
        require(account.balance() == 10_000, "Semaphore expected balance 10000, got " + account.balance());
        System.out.println("PASS semaphore: expected balance=$10000, actual=$" + account.balance());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
