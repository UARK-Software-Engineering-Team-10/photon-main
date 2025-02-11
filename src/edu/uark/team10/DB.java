package edu.uark.team10;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {

    // Database credentials
    final private static String jdbcUrl = "jdbc:postgresql://localhost:5432/photon";
    final private static String username = "student";
    final private static String password = "student";

    // Database instance and connection
    private static DB db = DB.get();
    private static Connection connection;

    public static DB get()
    {
        if (DB.db != null) return DB.db;

        DB db = new DB();

        try {
            // Register the PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Connect to the database
            DB.connection = DriverManager.getConnection(DB.jdbcUrl, DB.username, DB.password);

            // Get table
            DatabaseMetaData dbMeta = DB.connection.getMetaData();
            ResultSet resultSet = dbMeta.getTables(null, null, "playerdata", null);

            // Table does not exist
            if (!resultSet.next())
            {
                // Create table
                String query = "CREATE TABLE playerdata (playername VARCHAR(30), machineid int, score int);";
                query(query);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }  catch (SQLException e) {
            System.out.println("Could not make a connection to the database or some other database error.");
            e.printStackTrace();
        }

        DB.db = db;

        return db;
    }

    private static ResultSet query(String query)
    {
        try {
            System.out.println("Making a query: " + query);

            // Query the database
            Statement statement = DB.connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            statement.close();

            printTable();

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Prints the entire table (for dev purposes)
    private static void printTable()
    {
        String query = "SELECT * FROM playerdata;";
        ResultSet result = query(query);

        System.out.println(result.toString());
    }

    public void addPlayer(String playername, int machineId)
    {
        String query = "INSERT INTO playerdata VALUES (" + playername + ", " + machineId + ", 0);";
        query(query);

    }

    public void addScore(int machineId, int score)
    {
        String queryGet = "SELECT score FROM playerdata WHERE machineId = " + machineId + ";";
        ResultSet result = query(queryGet);

        int newScore = Integer.valueOf(result.toString()) + score;

        String queryUpdate = "UPDATE playerdata SET score = " + newScore + ";";
        query(queryUpdate);

    }

    public void shutdown()
    {
        try {
            DB.connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
