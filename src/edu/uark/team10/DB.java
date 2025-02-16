package edu.uark.team10;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

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
            ResultSet resultSet = dbMeta.getTables(null, null, "players", null);

            // Table does not exist
            if (!resultSet.next())
            {
                // Create table
                String query = "CREATE TABLE players (id INT, codename VARCHAR(30));";
                query(query).close();
            }

            resultSet.close();

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
            statement.execute(query);
            //statement.close();

            return statement.getResultSet();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Prints the entire table (for testing purposes)
    private static void printTable()
    {
        String query = "SELECT * FROM players;";
        ResultSet result = query(query);

        System.out.println("Printing players table:");
        try {
            while (result.next())
            {
                for (int i = 1; i <= 2; i++)
                {
                    if (i == 1)
                    {
                        System.out.print(result.getInt(i) + " ");
                    } else
                    {
                        System.out.print(result.getString(i) + " ");
                    }
                    
                }

                System.out.println("");
            }

            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }

    public void addEntry(int id, String codename)
    {
        String query = "";
        String dbCodename = getCodename(id);
        if (dbCodename == null)
        {
            query = "INSERT INTO players (id, codename) VALUES (" + id + ", '" + codename + "');";
        } else if (dbCodename.equals(codename))
        {
            System.out.println("id:codename pair exists already: " + id + ":" + codename);
            printTable();
            return;
        } else
        {
            query = "UPDATE players SET codename = '" + codename + "' WHERE id = " + id + ";";
        }

        query(query);
        printTable();
    }

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

    public HashMap<Integer, String> getAllEntries()
    {
        HashMap<Integer, String> entries = new HashMap<>();

        String query = "SELECT * FROM players;";
        ResultSet result = query(query);

        try {
            while (result.next())
            {
                int id = 0;
                String codename = "";

                for (int i = 1; i <= 2; i++)
                {
                    if (i == 1)
                    {
                        id = result.getInt(i);
                    } else
                    {
                        codename = result.getString(i);
                    }
                    
                }

                entries.put(id, codename);
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
