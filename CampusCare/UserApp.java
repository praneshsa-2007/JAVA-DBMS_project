import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * UserApp
 * Student Portal for Student Complaint Registration System (complaint_db).
 * Features:
 * 1. Register a New Complaint
 * 2. View Previous Complaints by Student ID (Feature 1)
 */
public class UserApp {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        showMenu();
    }

    public static void showMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("     CAMPUSCARE - STUDENT PORTAL  ");
            System.out.println("=================================");
            System.out.println("1. Register a New Complaint");
            System.out.println("2. View My Previous Complaints (by Student ID)");
            System.out.println("3. Exit");
            System.out.print("Enter choice (1-3): ");

            String choice = SCANNER.nextLine().trim();
            switch (choice) {
                case "1":
                    registerComplaint();
                    break;
                case "2":
                    viewComplaintsByStudentId();
                    break;
                case "3":
                    System.out.println("Exiting Student Portal. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1-3.");
            }
        }
    }

  
    private static void registerComplaint() {
        System.out.println("\n--- Submit a New Complaint ---");

        String studentId = readRequired("Enter Student ID(Roll no): ");
        String studentName = readRequired("Enter Student Name: ");


        saveOrUpdateStudent(studentId, studentName);

        String category = readCategory();
        String location = readRequired("Enter Location: ");
        String description = readRequired("Enter Complaint Description: ");

        String insertComplaintSql = "INSERT INTO complaints (student_id, category, location, description, status) "
                                  + "VALUES (?, ?, ?, ?, 'PENDING')";

        String insertHistorySql = "INSERT INTO complaint_history (complaint_id, old_status, new_status, remarks) "
                                + "VALUES (?, NULL, 'PENDING', 'Complaint lodged by student')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            int complaintId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(insertComplaintSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, studentId);
                stmt.setString(2, category);
                stmt.setString(3, location);
                stmt.setString(4, description);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        complaintId = rs.getInt(1);
                    }
                }
            }

            if (complaintId > 0) {
                try (PreparedStatement histStmt = conn.prepareStatement(insertHistorySql)) {
                    histStmt.setInt(1, complaintId);
                    histStmt.executeUpdate();
                }

                conn.commit(); // Commit transaction
                System.out.println("\nSUCCESS: Complaint submitted successfully!");
                System.out.println("Your Complaint ID is: " + complaintId);
                System.out.println("Status: PENDING");
                System.out.println("Please save this ID to track your complaint status.");
            } else {
                conn.rollback();
                System.out.println("Failed to generate complaint ID. Submission rolled back.");
            }

        } catch (SQLException e) {
            System.out.println("Database error while registering complaint: " + e.getMessage());
        }
    }

    private static void viewComplaintsByStudentId() {
        System.out.println("\n--- View Previous Complaints by Student ID ---");
        String studentId = readRequired("Enter your Student ID: ");

        String sql = "SELECT c.complaint_id, c.student_id, s.name AS student_name, "
                   + "       c.category, c.location, c.description, c.status, c.created_at, "
                   + "       st.name AS staff_name, st.department AS staff_dept "
                   + "FROM complaints c "
                   + "JOIN students s ON c.student_id = s.student_id "
                   + "LEFT JOIN staff st ON c.assigned_staff_id = st.staff_id "
                   + "WHERE c.student_id = ? "
                   + "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    if (!found) {
                        System.out.println("\nComplaints registered for Student: " + rs.getString("student_name") 
                                           + " (" + studentId + ")");
                        System.out.println("========================================================================");
                    }
                    found = true;
                    printComplaintSummary(rs);
                }

                if (!found) {
                    System.out.println("No complaints found for Student ID: " + studentId);
                    return;
                }
            }

            // Option to view detailed history/timeline
            System.out.print("\nWould you like to view timeline/history for a complaint? (Enter Complaint ID or press Enter to skip): ");
            String idInput = SCANNER.nextLine().trim();
            if (!idInput.isEmpty()) {
                try {
                    int cId = Integer.parseInt(idInput);
                    viewComplaintHistory(cId);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid complaint ID.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Displays chronological status changes and remarks for a complaint.
     */
    private static void viewComplaintHistory(int complaintId) {
        String historySql = "SELECT old_status, new_status, remarks, changed_at "
                          + "FROM complaint_history "
                          + "WHERE complaint_id = ? "
                          + "ORDER BY changed_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(historySql)) {

            stmt.setInt(1, complaintId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n>>> STATUS TIMELINE & REMARKS for Complaint #" + complaintId + " <<<");
                boolean hasHistory = false;
                while (rs.next()) {
                    hasHistory = true;
                    String oldS = rs.getString("old_status") == null ? "START" : rs.getString("old_status");
                    String newS = rs.getString("new_status");
                    String rem = rs.getString("remarks") == null ? "None" : rs.getString("remarks");
                    String time = rs.getString("changed_at");

                    System.out.println("  [" + time + "] " + oldS + " -> " + newS + " | Remarks: " + rem);
                }
                if (!hasHistory) {
                    System.out.println("  No history records found.");
                }
                System.out.println("------------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching history: " + e.getMessage());
        }
    }

    private static void printComplaintSummary(ResultSet rs) throws SQLException {
        String assignedStaff = rs.getString("staff_name");
        String staffDisplay = (assignedStaff != null) 
                            ? assignedStaff + " (" + rs.getString("staff_dept") + ")"
                            : "Not Yet Assigned";

        System.out.println("Complaint ID   : #" + rs.getInt("complaint_id"));
        System.out.println("Category       : " + rs.getString("category"));
        System.out.println("Location       : " + rs.getString("location"));
        System.out.println("Description    : " + rs.getString("description"));
        System.out.println("Current Status : " + rs.getString("status"));
        System.out.println("Assigned Staff : " + staffDisplay);
        System.out.println("Submitted At   : " + rs.getTimestamp("created_at"));
        System.out.println("------------------------------------------------------------------------");
    }

    private static void saveOrUpdateStudent(String studentId, String studentName) {
        String sql = "INSERT INTO students (student_id, name) VALUES (?, ?) "
                   + "ON DUPLICATE KEY UPDATE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setString(2, studentName);
            stmt.setString(3, studentName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Warning: Could not save student details: " + e.getMessage());
        }
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty. Please try again.");
        }
    }

    private static String readCategory() {
        String[] categories = {
            "Classroom Facilities",
            "Wifi / Internet",
            "Water supply",
            "Cleanliness",
            "Electrical Issue",
            "Other"
        };
        System.out.println("\nSelect Complaint Category:");
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i + 1) + ". " + categories[i]);
        }
        while (true) {
            System.out.print("Choose category (1-" + categories.length + "): ");
            String input = SCANNER.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= categories.length) {
                    return categories[choice - 1];
                }
            } catch (NumberFormatException ignored) { }
            System.out.println("Invalid choice. Please enter a number between 1 and " + categories.length + ".");
        }
    }
}
