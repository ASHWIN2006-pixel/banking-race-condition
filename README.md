# Banking Race Condition Investigation

A dependency-free Java console project that demonstrates a lost update in a banking account and compares two correct locking approaches: Java's `synchronized` monitor (mutex-style lock) and a binary `Semaphore`.

## Requirements

- JDK 19 or newer (JDK 21 recommended)
- VS Code with the Extension Pack for Java, or a terminal
- No database, build tool, or external libraries required

## Project layout

```text
BankingRaceConditionInvestigation/
├── .gitignore
├── README.md
├── logs/
│   └── .gitkeep
├── src/
│   ├── main/java/bankingrace/
│   │   ├── BankAccount.java
│   │   ├── ConcurrencyTester.java
│   │   ├── Main.java
│   │   └── TransactionLogger.java
│   └── test/java/bankingrace/
│       └── ConcurrencyTests.java
└── .vscode/
    └── launch.json
```

## Run from a terminal

Open a terminal in this project directory and run:

```sh
mkdir -p out
javac -d out src/main/java/bankingrace/*.java src/test/java/bankingrace/*.java
java -cp out bankingrace.Main
```

On Windows PowerShell, replace `mkdir -p out` with `New-Item -ItemType Directory -Force out` (or omit it; `javac -d out` creates the destination).

Run the test entry point separately:

```sh
java -cp out bankingrace.ConcurrencyTests
```

## Run in VS Code

1. Install a JDK (21 recommended) and the **Extension Pack for Java**.
2. Open this project folder in VS Code.
3. Open `src/main/java/bankingrace/Main.java` and select **Run Java** above `main`.
4. To run the checks, open `src/test/java/bankingrace/ConcurrencyTests.java` and select **Run Java**.

The included `.vscode/launch.json` also provides launch configurations for the demo and checks.

## What the checks demonstrate

- **Unsafe:** two threads are deliberately held after reading the same initial balance. Both then write their own calculated value, so two $100 deposits from $0 deterministically leave $100 instead of $200. This controlled interleaving exposes the lost update every time.
- **Synchronized:** eight threads each deposit and withdraw $1 500 times. Starting from $10,000, the expected final balance is exactly $10,000.
- **Semaphore:** repeats the same concurrent workload protected by a fair binary semaphore; expected final balance is $10,000.

An unsafe race in ordinary, uncontrolled workloads is scheduling-dependent; the controlled test makes the defect reproducible. The checks use assertions and exit with an error if a result differs from its expected balance.

## Transaction logs

Each transaction records an ISO-8601 timestamp, thread name and ID, implementation mode, action, amount, and balance before/after. The synchronized logger writes to the console and appends to `logs/transactions.log` when running the demo, or `logs/test-transactions.log` for the test entry point. The `logs/` directory is committed with `.gitkeep`; generated logs are ignored by Git.

## Investigation notes

The balance read, calculation, and write form a critical section. A race condition exists when multiple threads access that shared state and at least one modifies it without mutual exclusion. Useful production evidence includes transaction/account IDs, unique transaction IDs, operation type and amount, request and completion timestamps, thread/request IDs, balance before/after, lock acquisition/release timing, retries, and errors. Avoid logging credentials, full card numbers, or other secrets.

## Upload to GitHub

1. Extract the ZIP.
2. Create a new empty GitHub repository.
3. In a terminal opened in the extracted project folder, run:

```sh
git init
git add .
git commit -m "Add banking race condition investigation project"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
git push -u origin main
```

Replace the remote URL with the URL of your repository. Alternatively, create a repository on GitHub and upload the extracted project files through the web interface.
