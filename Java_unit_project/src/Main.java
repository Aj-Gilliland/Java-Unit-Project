import java.sql.*;

public class Main {

    public static Integer testConnect() {
        try {
            // Explicitly load the PostgreSQL JDBC driver class
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/thesafe", "aj", "aj1274414");
            // It's a good practice to close the connection
            connection.close();
            System.out.println("Connection successful");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());
        }
        return 8;
    }

    public static void main(String[] args) {
        System.out.println("Hello World");
        testConnect();
        System.out.println("The program has finished!!!");
    }
}
