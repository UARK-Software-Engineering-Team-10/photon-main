package edu.uark.team10;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {

    public static DB getDB()
    {
        DB db = new DB();

        String jdbcUrl = "jdbc:postgresql://localhost:5432/database_name";
        String username = "username";
        String password = "password";

        Connection connection

        // Register the PostgreSQL driver
        try {
            Class.forName("org.postgresql.Driver");

            // Connect to the database
            connection = DriverManager.getConnection(jdbcUrl, username, password);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }  catch (SQLException e) {
            e.printStackTrace();
        }

        // Perform desired database operations
        Statement statement = connection.createStatement();
        String query = "SELECT * FROM students";
        ResultSet resultSet = statement.executeQuery(query);

        // Close the connection
        resultSet.close();
        statement.close();
        connection.close();

        return db;
    }


    
}
