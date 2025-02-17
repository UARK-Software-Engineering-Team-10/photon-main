package edu.uark.team10;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * A class that interfaces with the PostgreSQL server.
 * Use the static get() method to get an instance instead of
 * calling new.
 */
public class DB {

    // Database credentials
    final private static String jdbcUrl = "jdbc:postgresql://localhost:5432/photon";
    final private static String username = "student";
    final private static String password = "student";

    // Database instance and connection
    private static DB db = DB.get();
    private static Connection connection;

    /**
     * Get the database instance. Creates a connection to the database
     * if it isn't already connected. Creates the player table if
     * it isn't already created.
     * 
     * @return The databse instance
     */
    public static DB get()
    {
        // Return the instance if it is created already
        if (DB.db != null) return DB.db;

        DB db = new DB();

        try {
            // Register the PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Connect to the database
            DB.connection = DriverManager.getConnection(DB.jdbcUrl, DB.username, DB.password);

            // Get table
            DatabaseMetaData dbMeta = DB.connection.getMetaData();
            ResultSet resultSet = dbMeta.getTables(null, null, "players", null);

            // Table does not exist
            if (!resultSet.next())
            {
                // Create table
                String query = "CREATE TABLE players (id INT, codename VARCHAR(30));";
                query(query).close();
            }

            resultSet.close();

            // Set DB instance if all goes well
            DB.db = db;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }  catch (SQLException e) {
            System.out.println("Could not make a connection to the database or some other database error.");
            e.printStackTrace();
        }

        return db;
    }

    /**
     * Query the database and get the result set.
     * Does not check for valid queries. Does not
     * close the result set.
     * 
     * @param query
     * @return A nullable ResultSet of the query
     */
    private static ResultSet query(String query)
    {
        try {
            System.out.println("Making a query: " + query);

            // Query the database
            Statement statement = DB.connection.createStatement();
            statement.execute(query);

            return statement.getResultSet();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Error
    }

    /**
     * Prints the entire table for testing purposes
     */
    private static void printTable()
    {
        String query = "SELECT * FROM players;";
        ResultSet result = query(query);

        System.out.println("Printing players table:");
        try {
            while (result.next()) // While there is another row
            {
                for (int i = 1; i <= 2; i++)
                {
                    if (i == 1) // Player ID
                    {
                        System.out.print(result.getInt(i) + " ");
                    } else if (i == 2) // Codename
                    {
                        System.out.print(result.getString(i) + " ");
                    }
                    
                }

                System.out.println(""); // Newline
            }

            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Add a row to the database. Updates the row if player ID
     * is present, otherwise inserts.
     * 
     * @param id
     * @param codename
     */
    public void addEntry(int id, String codename)
    {
        String query = "";

        String dbCodename = getCodename(id); // Check if the player ID exists
        if (dbCodename == null) // ID does not exist: insert
        {
            query = "INSERT INTO players (id, codename) VALUES (" + id + ", '" + codename + "');";
        } else if (dbCodename.equals(codename)) // ID exists, codename is the same, do nothing
        {
            System.out.println("id:codename pair exists already: " + id + ":" + codename);
            printTable();
            return; // exit
        } else // ID exists: update
        {
            query = "UPDATE players SET codename = '" + codename + "' WHERE id = " + id + ";";
        }

        query(query);
        printTable();
    }

    /**
     * Get the codename of the player ID if it exists.
     * 
     * @param id
     * @return The codename or null
     */
    public String getCodename(int id)
    {
        String codename = null;
        try {
            String query = "SELECT codename FROM players WHERE id = " + id + ";";
            ResultSet result = query(query);

            if (!result.next()) return codename; // True if id not found

            codename = result.getString(1);
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return codename;
    }

    /**
     * Get the table as a hashmap.
     * 
     * @return
     */
    public HashMap<Integer, String> getAllEntries()
    {
        HashMap<Integer, String> entries = new HashMap<>();

        String query = "SELECT * FROM players;";
        ResultSet result = query(query);

        try {
            while (result.next()) // While there is another row
            {
                int id = 0;
                String codename = "";

                for (int i = 1; i <= 2; i++)
                {
                    if (i == 1) // Player ID
                    {
                        id = result.getInt(i);
                    } else // Codename
                    {
                        codename = result.getString(i);
                    }
                    
                }

                entries.put(id, codename); // Add row to hashmap
            }

            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entries;
    }

    /*
    public void removeEntry(int id)
    {
        String query = "DELETE FROM players WHERE id = " + id + ";";
        query(query);
        printTable();
    }
    */

    /*
    public void shutdown()
    {
        try {
            DB.connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    */

}
