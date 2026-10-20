import java.util.Scanner;

/**
 * Main
 * Unified dual-portal entry point for CampusCare (complaint_db).
 * 1. Student Portal
 * 2. Staff Portal
 * 3. View Staff Accounts & Credentials
 * 4. Exit
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("            WELCOME TO CAMPUSCARE                 ");
        System.out.println("      Student Complaint Registration System       ");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            System.out.println("\n================ MAIN PORTAL ================");
            System.out.println("1. Student Portal (Submit & Track Complaints)");
            System.out.println("2. Staff Portal (Manage, Filter & Resolve)");
            System.out.println("3. Exit");
            System.out.print("Enter your choice (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    UserApp.showMenu();
                    break;
                case "2":
                    AdminApp.showMenu();
                    break;
                case "3":
                    System.out.println("\nThank you for using CampusCare. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1-3.");
            }
        }
    }
}
