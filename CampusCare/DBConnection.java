import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection
 * Manages JDBC connection to the complaint_db database.
 */
public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/complaint_db";
    private static final String USER = "root";
    private static final String PASSWORD = "2015672754"; // MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in classpath.");
        }
    }

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
