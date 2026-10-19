import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class AdminApp {
    private static final String ADMIN_USERNAME = "user";
    private static final String ADMIN_PASSWORD = "1234";
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("      CAMPUSCARE - ADMIN");
        System.out.println("=================================");

        if (!login()) {
            System.out.println("Too many failed login attempts. Exiting.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("----------- ADMIN MENU -----------");
            System.out.println("1. View all complaints");
            System.out.println("2. Search complaint by ID");
            System.out.println("3. Update complaint status");
            System.out.println("4. Delete complaint");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            String choice = SCANNER.nextLine().trim();
            switch (choice) {
                case "1": viewAllComplaints(); break;
                case "2": searchComplaint(); break;
                case "3": updateStatus(); break;
                case "4": deleteComplaint(); break;
                case "5":
                    running = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1-5.");
            }
        }
    }

    private static boolean login() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("Admin username: ");
            String username = SCANNER.nextLine().trim();
            System.out.print("Admin password: ");
            String password = SCANNER.nextLine();

            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                System.out.println("Login successful. Welcome, Administrator!");
                return true;
            }
            System.out.println("Incorrect username or password. Attempts remaining: " + (3 - attempt));
        }
        return false;
    }

    private static void viewAllComplaints() {
        String sql = "SELECT id, student_name, category, location, description, status, created_at "
                   + "FROM complaints ORDER BY created_at DESC, id DESC";
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            System.out.println();
            System.out.println("--------------- ALL COMPLAINTS ---------------");
            boolean found = false;
            while (result.next()) {
                found = true;
                printComplaint(result);
                System.out.println("-----------------------------------------------");
            }
            if (!found) System.out.println("No complaints have been registered yet.");
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private static void searchComplaint() {
        int id = readPositiveId("Enter complaint ID: ");
        String sql = "SELECT id, student_name, category, location, description, status, created_at "
                   + "FROM complaints WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) printComplaint(result);
                else System.out.println("No complaint found with ID " + id + ".");
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private static void updateStatus() {
        int id = readPositiveId("Enter complaint ID to update: ");
        System.out.println("1. Pending");
        System.out.println("2. In Progress");
        System.out.println("3. Resolved");
        System.out.print("Choose new status (1-3): ");
        String choice = SCANNER.nextLine().trim();

        String status;
        switch (choice) {
            case "1": status = "Pending"; break;
            case "2": status = "In Progress"; break;
            case "3": status = "Resolved"; break;
            default:
                System.out.println("Invalid status choice. No changes made.");
                return;
        }

        String sql = "UPDATE complaints SET status = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, id);
            int changed = statement.executeUpdate();
            if (changed == 1) System.out.println("Complaint #" + id + " status updated to " + status + ".");
            else System.out.println("No complaint found with ID " + id + ".");
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private static void deleteComplaint() {
        int id = readPositiveId("Enter complaint ID to delete: ");
        System.out.print("Delete complaint #" + id + "? Type YES to confirm: ");
        String confirmation = SCANNER.nextLine().trim();
        if (!"YES".equals(confirmation)) {
            System.out.println("Delete cancelled.");
            return;
        }

        String sql = "DELETE FROM complaints WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int deleted = statement.executeUpdate();
            if (deleted == 1) System.out.println("Complaint deleted successfully.");
            else System.out.println("No complaint found with ID " + id + ".");
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private static int readPositiveId(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                int id = Integer.parseInt(input);
                if (id > 0) return id;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a valid positive numeric ID.");
        }
    }

    private static void printComplaint(ResultSet result) throws SQLException {
        System.out.println("Complaint ID : " + result.getInt("id"));
        System.out.println("Student Name : " + result.getString("student_name"));
        System.out.println("Category     : " + result.getString("category"));
        System.out.println("Location     : " + result.getString("location"));
        System.out.println("Description  : " + result.getString("description"));
        System.out.println("Status       : " + result.getString("status"));
        System.out.println("Created At   : " + result.getTimestamp("created_at"));
    }

    private static void showDatabaseError(SQLException e) {
        System.out.println("A database operation failed.");
        System.out.println("Check MySQL is running, the database/table exists, and DBConnection.java settings are correct.");
        System.out.println("Details: " + e.getMessage());
    }
}
