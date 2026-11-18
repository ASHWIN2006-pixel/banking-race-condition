package bankingrace;

import java.util.concurrent.Semaphore;

/** Three deliberately distinct approaches to protecting one shared balance. */
public final class BankAccount {
    private long balance;
    private final TransactionLogger logger;
    private final Semaphore semaphore = new Semaphore(1, true);

    public BankAccount(long openingBalance, TransactionLogger logger) {
        if (openingBalance < 0) throw new IllegalArgumentException("Opening balance cannot be negative");
        this.balance = openingBalance;
        this.logger = logger;
    }

    public long balance() { return balance; }

    /** Unsafe read-modify-write, exposed for a controlled race demonstration. */
    public void unsafeChange(long amount, String action, Runnable afterRead) {
        long before = balance;
        if (afterRead != null) afterRead.run();
        long after = calculate(before, amount, action);
        balance = after;
        logger.log("UNSAFE", action, amount, before, after);
    }

    /** Java intrinsic monitor, serving as a mutex around the critical section. */
    public synchronized void synchronizedChange(long amount, String action) {
        long before = balance;
        long after = calculate(before, amount, action);
        balance = after;
        logger.log("SYNCHRONIZED", action, amount, before, after);
    }

    /** Binary semaphore lock around the same critical section. */
    public void semaphoreChange(long amount, String action) throws InterruptedException {
        semaphore.acquire();
        try {
            long before = balance;
            long after = calculate(before, amount, action);
            balance = after;
            logger.log("SEMAPHORE", action, amount, before, after);
        } finally {
            semaphore.release();
        }
    }

    private static long calculate(long balance, long amount, String action) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        return switch (action) {
            case "DEPOSIT" -> Math.addExact(balance, amount);
            case "WITHDRAW" -> {
                if (balance < amount) throw new IllegalStateException("Insufficient funds");
                yield balance - amount;
            }
            default -> throw new IllegalArgumentException("Action must be DEPOSIT or WITHDRAW");
        };
    }
}
