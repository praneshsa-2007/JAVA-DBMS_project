import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class UserApp {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("       CAMPUSCARE - USER");
        System.out.println("=================================");
        System.out.println("Submit a campus complaint below.");
        System.out.println();

        String studentName = readRequired("Student name: ");
        String category = readCategory();
        String location = readRequired("Campus location: ");
        String description = readRequired("Complaint description: ");

        String sql = "INSERT INTO complaints (student_name, category, location, description, status) "
                   + "VALUES (?, ?, ?, ?, 'Pending')";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, studentName);
            statement.setString(2, category);
            statement.setString(3, location);
            statement.setString(4, description);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 1) {
                try (java.sql.ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        System.out.println();
                        System.out.println("Complaint submitted successfully!");
                        System.out.println("Your complaint ID is: " + keys.getInt(1));
                        System.out.println("Please keep this ID if you need to contact the campus office.");
                    } else {
                        System.out.println("Complaint submitted successfully.");
                    }
                }
            } else {
                System.out.println("Complaint could not be submitted. Please try again.");
            }
        } catch (SQLException e) {
            System.out.println("Could not save the complaint.");
            System.out.println("Check that MySQL is running and DBConnection.java has the correct password.");
            System.out.println("Details: " + e.getMessage());
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
        String[] categories = {"Classroom Facilities", "Wi-Fi / Internet", "Water Supply",
                               "Electrical Issue", "Cleanliness", "Other"};
        System.out.println("Complaint category:");
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
            System.out.println("Invalid choice. Please enter a number from 1 to " + categories.length + ".");
        }
    }
}
