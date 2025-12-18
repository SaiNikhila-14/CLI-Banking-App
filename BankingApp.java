import java.sql.*;
import java.util.Scanner;

public class BankingApp {
    // Database Connection String (Creates a file named 'bank.db')
    private static final String DB_URL = "jdbc:sqlite:bank.db";
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // 1. Initialize Database
        initializeDatabase();

        // 2. Main Menu Loop
        while (true) {
            System.out.println("\n--- SQL FINTECH BANKING SYSTEM ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Check Balance");
            System.out.println("5. Exit");
            System.out.print("Select: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: createAccount(); break;
                case 2: performTransaction("DEPOSIT"); break;
                case 3: performTransaction("WITHDRAW"); break;
                case 4: checkBalance(); break;
                case 5: System.exit(0);
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // Connect to DB and Create Table if it doesn't exist
    private static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
                     "id TEXT PRIMARY KEY, " +
                     "name TEXT NOT NULL, " +
                     "balance REAL DEFAULT 0.0);";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    private static void createAccount() {
        System.out.print("Enter Account Number (ID): ");
        String id = scanner.nextLine();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        String sql = "INSERT INTO accounts(id, name, balance) VALUES(?, ?, 0.0)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Using PreparedStatements prevents SQL Injection
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
            System.out.println("Success: Account created for " + name);
            
        } catch (SQLException e) {
            System.out.println("Error creating account (ID might involve duplicate): " + e.getMessage());
        }
    }

    private static void performTransaction(String type) {
        System.out.print("Enter Account ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        // Logic for Deposit vs Withdraw
        String sqlCheck = "SELECT balance FROM accounts WHERE id = ?";
        String sqlUpdate = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck);
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {

            // Step 1: Get current balance
            pstmtCheck.setString(1, id);
            ResultSet rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                double newBalance = 0;

                if (type.equals("DEPOSIT")) {
                    newBalance = currentBalance + amount;
                } else {
                    if (currentBalance >= amount) {
                        newBalance = currentBalance - amount;
                    } else {
                        System.out.println("Insufficient funds!");
                        return;
                    }
                }

                // Step 2: Update balance
                pstmtUpdate.setDouble(1, newBalance);
                pstmtUpdate.setString(2, id);
                pstmtUpdate.executeUpdate();
                System.out.println("Transaction Successful! New Balance: $" + newBalance);
            } else {
                System.out.println("Account not found.");
            }

        } catch (SQLException e) {
            System.out.println("Transaction Error: " + e.getMessage());
        }
    }

    private static void checkBalance() {
        System.out.print("Enter Account ID: ");
        String id = scanner.nextLine();
        String sql = "SELECT name, balance FROM accounts WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("User: " + rs.getString("name"));
                System.out.println("Current Balance: $" + rs.getDouble("balance"));
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}