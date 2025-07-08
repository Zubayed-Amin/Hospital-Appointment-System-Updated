package hospitalappointmentsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author zubay
 */
public class ConnectionDB {
    public static Connection getConnection() {
        Connection connection = null;

        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hospital_appointment_system", "root", "");
            System.out.println("Database Connected");

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }

        return connection;
    }
}
