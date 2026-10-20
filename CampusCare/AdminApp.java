import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * AdminApp
 * Staff / Admin Portal for Student Complaint Registration System (complaint_db).
 * Features:
 * 1. Staff Login using username (admin_<staff_name>) and password ("1234") (Feature 3)
 * 2. Access complaints filtered by status: PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, or ALL (Feature 2)
 * 3. Assign complaints to staff members
 * 4. Update status & Complete Work, displaying the full resolution summary upon completion (Feature 3)
 * 5. View complete complaint history / timeline
 */
public class AdminApp {
    private static final Scanner SCANNER = new Scanner(System.in);

    // Current logged-in staff session
    private static int loggedInStaffId = -1;
    private static String loggedInStaffName = "";
    private static String loggedInStaffDept = "";
    private static String loggedInUsername = "";

    public static void main(String[] args) {
        showMenu();
    }

    public static void showMenu() {
        System.out.println("\n=================================");
        System.out.println("      CAMPUSCARE - STAFF PORTAL   ");
        System.out.println("=================================");

        if (!loginStaff()) {
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n================ STAFF MENU ================");
            System.out.println("Logged in as: " + loggedInStaffName + " (" + loggedInStaffDept + " | User: " + loggedInUsername + ")");
            System.out.println("--------------------------------------------");
            System.out.println("1. Access Complaint List by Status (Filter)");
            System.out.println("2. View Complaints Assigned to Me");
            System.out.println("3. Assign Complaint to Staff");
            System.out.println("4. Update Complaint Status & Complete Work");
            System.out.println("5. View Complete Complaint History");
            System.out.println("6. View Staff Accounts & Usernames");
            System.out.println("7. Logout");
            System.out.print("Enter choice (1-7): ");

            String choice = SCANNER.nextLine().trim();
            switch (choice) {
                case "1":
                    viewComplaintsByStatus();
                    break;
                case "2":
                    viewComplaintsAssignedToMe();
                    break;
                case "3":
                    assignComplaintToStaff();
                    break;
                case "4":
                    updateStatusAndCompleteWork();
                    break;
                case "5":
                    viewComplaintHistory();
                    break;
                case "6":
                    displayStaffCredentials();
                    break;
                case "7":
                    System.out.println("Logged out successfully.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select 1-7.");
            }
        }
    }

    /**
     * Feature 3: Staff login using username (e.g. admin_<staff_name>) and password ("1234").
     */
    private static boolean loginStaff() {
        System.out.println("\n--- Staff Authentication ---");
        System.out.println("(Tip: Enter 'help' to view all staff usernames and passwords)");

        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.print("Enter Username (e.g., admin_meena): ");
            String userInput = SCANNER.nextLine().trim();

            if (userInput.equalsIgnoreCase("help")) {
                displayStaffCredentials();
                System.out.println();
                continue;
            }

            System.out.print("Enter Password: ");
            String password = SCANNER.nextLine().trim();

            String sql = "SELECT staff_id, name, department, username FROM staff "
                       + "WHERE (username = ? OR CAST(staff_id AS CHAR) = ? OR name = ?) AND password = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, userInput);
                stmt.setString(2, userInput);
                stmt.setString(3, userInput);
                stmt.setString(4, password);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        loggedInStaffId = rs.getInt("staff_id");
                        loggedInStaffName = rs.getString("name");
                        loggedInStaffDept = rs.getString("department");
                        loggedInUsername = rs.getString("username");
                        System.out.println("\nSUCCESS: Login successful! Welcome, " + loggedInStaffName 
                                           + " [" + loggedInStaffDept + "]");
                        return true;
                    }
                }

            } catch (SQLException e) {
                System.out.println("Database error during login: " + e.getMessage());
                return false;
            }

            System.out.println("Invalid credentials. Remaining attempts: " + (3 - attempt));
        }

        System.out.println("Too many failed attempts. Returning to main menu.");
        return false;
    }

    /**
     * Feature 2: Access complaint list filtered by status.
     */
    private static void viewComplaintsByStatus() {
        System.out.println("\n--- Filter Complaints by Status ---");
        System.out.println("1. PENDING");
        System.out.println("2. ASSIGNED");
        System.out.println("3. IN_PROGRESS");
        System.out.println("4. RESOLVED");
        System.out.println("5. ALL COMPLAINTS");
        System.out.print("Select status filter (1-5): ");

        String filterChoice = SCANNER.nextLine().trim();
        String statusFilter = null;

        switch (filterChoice) {
            case "1": statusFilter = "PENDING"; break;
            case "2": statusFilter = "ASSIGNED"; break;
            case "3": statusFilter = "IN_PROGRESS"; break;
            case "4": statusFilter = "RESOLVED"; break;
            case "5": statusFilter = null; break; // All
            default:
                System.out.println("Invalid selection. Returning to menu.");
                return;
        }

        String sql = "SELECT c.complaint_id, c.student_id, s.name AS student_name, "
                   + "       c.category, c.location, c.description, c.status, c.created_at, "
                   + "       st.name AS staff_name, st.department AS staff_dept "
                   + "FROM complaints c "
                   + "JOIN students s ON c.student_id = s.student_id "
                   + "LEFT JOIN staff st ON c.assigned_staff_id = st.staff_id ";

        if (statusFilter != null) {
            sql += "WHERE c.status = ? ";
        }
        sql += "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (statusFilter != null) {
                stmt.setString(1, statusFilter);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n================ COMPLAINTS LIST [" + (statusFilter == null ? "ALL" : statusFilter) + "] ================");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    printComplaintCard(rs);
                }
                if (!found) {
                    System.out.println("No complaints found with status: " + (statusFilter == null ? "ALL" : statusFilter));
                }
                System.out.println("==========================================================================");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Views complaints assigned to the currently logged in staff member.
     */
    private static void viewComplaintsAssignedToMe() {
        String sql = "SELECT c.complaint_id, c.student_id, s.name AS student_name, "
                   + "       c.category, c.location, c.description, c.status, c.created_at, "
                   + "       st.name AS staff_name, st.department AS staff_dept "
                   + "FROM complaints c "
                   + "JOIN students s ON c.student_id = s.student_id "
                   + "JOIN staff st ON c.assigned_staff_id = st.staff_id "
                   + "WHERE c.assigned_staff_id = ? "
                   + "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loggedInStaffId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n================ MY ASSIGNED COMPLAINTS ================");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    printComplaintCard(rs);
                }
                if (!found) {
                    System.out.println("No complaints currently assigned to you (" + loggedInStaffName + ").");
                }
                System.out.println("========================================================");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Assigns a complaint to a staff member.
     */
    private static void assignComplaintToStaff() {
        System.out.println("\n--- Assign Complaint to Staff ---");
        System.out.print("Enter Complaint ID to assign: ");
        String idInput = SCANNER.nextLine().trim();

        int complaintId;
        try {
            complaintId = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid complaint ID.");
            return;
        }

        // Verify complaint exists and get current status
        String checkSql = "SELECT status, assigned_staff_id FROM complaints WHERE complaint_id = ?";
        String currentStatus;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, complaintId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No complaint found with ID: " + complaintId);
                    return;
                }
                currentStatus = rs.getString("status");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

        System.out.println("\nAssign to:");
        System.out.println("1. Assign to Myself (" + loggedInStaffName + " - " + loggedInStaffDept + ")");
        System.out.println("2. Select another Staff Member");
        System.out.print("Choose option (1-2): ");
        String opt = SCANNER.nextLine().trim();

        int targetStaffId = loggedInStaffId;
        String targetStaffName = loggedInStaffName;

        if (opt.equals("2")) {
            displayStaffList();
            System.out.print("Enter Staff ID to assign to: ");
            String staffInput = SCANNER.nextLine().trim();
            try {
                targetStaffId = Integer.parseInt(staffInput);
                targetStaffName = getStaffNameById(targetStaffId);
                if (targetStaffName == null) {
                    System.out.println("Invalid Staff ID. Assignment cancelled.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid Staff ID.");
                return;
            }
        }

        String updateSql = "UPDATE complaints SET assigned_staff_id = ?, status = 'ASSIGNED' WHERE complaint_id = ?";
        String historySql = "INSERT INTO complaint_history (complaint_id, old_status, new_status, remarks) "
                          + "VALUES (?, ?, 'ASSIGNED', ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement uStmt = conn.prepareStatement(updateSql);
                 PreparedStatement hStmt = conn.prepareStatement(historySql)) {

                uStmt.setInt(1, targetStaffId);
                uStmt.setInt(2, complaintId);
                uStmt.executeUpdate();

                hStmt.setInt(1, complaintId);
                hStmt.setString(2, currentStatus);
                hStmt.setString(3, "Assigned to staff: " + targetStaffName + " (Staff ID: " + targetStaffId + ")");
                hStmt.executeUpdate();

                conn.commit();
                System.out.println("\nSUCCESS: Complaint #" + complaintId + " assigned to " + targetStaffName + ".");
                System.out.println("Status updated to: ASSIGNED");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Feature 3: Updates complaint status and records work completion remarks.
     * When completed, immediately displays the updated complaint details and completion summary.
     */
    private static void updateStatusAndCompleteWork() {
        System.out.println("\n--- Update Complaint Status & Complete Work ---");
        System.out.print("Enter Complaint ID: ");
        String idInput = SCANNER.nextLine().trim();

        int complaintId;
        try {
            complaintId = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid complaint ID.");
            return;
        }

        // Fetch current complaint state
        String fetchSql = "SELECT c.complaint_id, c.student_id, s.name AS student_name, "
                        + "       c.category, c.location, c.description, c.status, c.assigned_staff_id "
                        + "FROM complaints c "
                        + "JOIN students s ON c.student_id = s.student_id "
                        + "WHERE c.complaint_id = ?";

        String currentStatus;
        int currentStaffId;
        String studentName;
        String category;
        String location;
        String description;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(fetchSql)) {

            stmt.setInt(1, complaintId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No complaint found with ID: " + complaintId);
                    return;
                }
                currentStatus = rs.getString("status");
                currentStaffId = rs.getInt("assigned_staff_id");
                studentName = rs.getString("student_name");
                category = rs.getString("category");
                location = rs.getString("location");
                description = rs.getString("description");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

        System.out.println("\nCurrent Status: " + currentStatus);
        System.out.println("Select New Status:");
        System.out.println("1. IN_PROGRESS (Start working on complaint)");
        System.out.println("2. RESOLVED    (Work completed)");
        System.out.print("Choose status (1-2): ");

        String choice = SCANNER.nextLine().trim();
        String newStatus;
        if (choice.equals("1")) {
            newStatus = "IN_PROGRESS";
        } else if (choice.equals("2")) {
            newStatus = "RESOLVED";
        } else {
            System.out.println("Invalid selection. Operation cancelled.");
            return;
        }

        System.out.print("Enter Work Remarks / Resolution Notes: ");
        String remarks = SCANNER.nextLine().trim();
        if (remarks.isEmpty()) {
            remarks = (newStatus.equals("RESOLVED")) ? "Work completed by staff." : "Work initiated by staff.";
        }

        remarks = remarks + " [Updated by " + loggedInStaffName + "]";

        int staffToAssign = (currentStaffId == 0) ? loggedInStaffId : currentStaffId;

        String updateSql = "UPDATE complaints SET status = ?, assigned_staff_id = ? WHERE complaint_id = ?";
        String historySql = "INSERT INTO complaint_history (complaint_id, old_status, new_status, remarks) "
                          + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement uStmt = conn.prepareStatement(updateSql);
                 PreparedStatement hStmt = conn.prepareStatement(historySql)) {

                uStmt.setString(1, newStatus);
                uStmt.setInt(2, staffToAssign);
                uStmt.setInt(3, complaintId);
                uStmt.executeUpdate();

                hStmt.setInt(1, complaintId);
                hStmt.setString(2, currentStatus);
                hStmt.setString(3, newStatus);
                hStmt.setString(4, remarks);
                hStmt.executeUpdate();

                conn.commit();

                // "later completing the work display it" -> Display full completed work details
                System.out.println("\n========================================================");
                System.out.println("               WORK STATUS UPDATED                      ");
                System.out.println("========================================================");
                System.out.println("Complaint ID    : #" + complaintId);
                System.out.println("Student Name    : " + studentName);
                System.out.println("Category        : " + category);
                System.out.println("Location        : " + location);
                System.out.println("Description     : " + description);
                System.out.println("Previous Status : " + currentStatus);
                System.out.println("New Status      : " + newStatus);
                System.out.println("Handled By      : " + loggedInStaffName + " (" + loggedInStaffDept + ")");
                System.out.println("Work Remarks    : " + remarks);
                if (newStatus.equals("RESOLVED")) {
                    System.out.println("STATUS          : *** WORK COMPLETED & RESOLVED ***");
                }
                System.out.println("========================================================");

                // Display full timeline
                viewComplaintHistoryById(complaintId);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Database error while updating status: " + e.getMessage());
        }
    }

    private static void viewComplaintHistory() {
        System.out.print("\nEnter Complaint ID: ");
        String idInput = SCANNER.nextLine().trim();
        try {
            int cId = Integer.parseInt(idInput);
            viewComplaintHistoryById(cId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid complaint ID.");
        }
    }

    private static void viewComplaintHistoryById(int complaintId) {
        String historySql = "SELECT old_status, new_status, remarks, changed_at "
                          + "FROM complaint_history "
                          + "WHERE complaint_id = ? "
                          + "ORDER BY changed_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(historySql)) {

            stmt.setInt(1, complaintId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println(">>> AUDIT TIMELINE FOR COMPLAINT #" + complaintId + " <<<");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    String oldS = rs.getString("old_status") == null ? "START" : rs.getString("old_status");
                    String newS = rs.getString("new_status");
                    String rem = rs.getString("remarks") == null ? "None" : rs.getString("remarks");
                    String time = rs.getString("changed_at");

                    System.out.println("  [" + time + "] " + oldS + " -> " + newS + " | Remarks: " + rem);
                }
                if (!found) {
                    System.out.println("  No history records found.");
                }
                System.out.println("------------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching history: " + e.getMessage());
        }
    }

    /**
     * Displays all staff accounts, departments, usernames, and passwords for reference.
     */
    private static void displayStaffCredentials() {
        String sql = "SELECT staff_id, name, department, username, password FROM staff ORDER BY staff_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n================= REGISTERED STAFF ACCOUNTS =================");
            System.out.printf("%-10s %-18s %-22s %-16s %-10s%n", "Staff ID", "Name", "Department", "Username", "Password");
            System.out.println("-----------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-10d %-18s %-22s %-16s %-10s%n",
                    rs.getInt("staff_id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getString("username"),
                    rs.getString("password"));
            }
            System.out.println("=============================================================================");

        } catch (SQLException e) {
            System.out.println("Error reading staff accounts: " + e.getMessage());
        }
    }

    private static void displayStaffList() {
        String sql = "SELECT staff_id, name, department, username FROM staff ORDER BY staff_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nAvailable Staff Members:");
            while (rs.next()) {
                System.out.println("  ID " + rs.getInt("staff_id") + " : " + rs.getString("name") 
                                   + " (" + rs.getString("department") + " | " + rs.getString("username") + ")");
            }
        } catch (SQLException e) {
            System.out.println("Error listing staff: " + e.getMessage());
        }
    }

    private static String getStaffNameById(int staffId) {
        String sql = "SELECT name FROM staff WHERE staff_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("name");
            }
        } catch (SQLException ignored) { }
        return null;
    }

    private static void printComplaintCard(ResultSet rs) throws SQLException {
        String staffName = rs.getString("staff_name");
        String staffInfo = (staffName != null) ? staffName + " (" + rs.getString("staff_dept") + ")" : "Unassigned";

        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Complaint ID : #" + rs.getInt("complaint_id") + " | Status: " + rs.getString("status"));
        System.out.println("Student      : " + rs.getString("student_name") + " (ID: " + rs.getString("student_id") + ")");
        System.out.println("Category     : " + rs.getString("category"));
        System.out.println("Location     : " + rs.getString("location"));
        System.out.println("Description  : " + rs.getString("description"));
        System.out.println("Assigned To  : " + staffInfo);
        System.out.println("Created At   : " + rs.getTimestamp("created_at"));
    }
}
