package edu.uark.team10;

import java.util.ArrayList;
import java.util.Objects;

import javax.swing.table.AbstractTableModel;

/*
 * A custom table model class for the team tables in the player entry screen
 */
public class PlayerEntryTableModel extends AbstractTableModel
{
    // Static lists that store all machine IDs and player names.
    // They are used to check for duplicates
    private static ArrayList<Object> machineIds = new ArrayList<>();
    private static ArrayList<Object> playernames = new ArrayList<>();

    // Basic table data
    private final int maxPlayers = 20;
    private String[] columnNames = new String[] {"", "Machine ID", "Playername"};
    private Object[][] players = new Object[maxPlayers][columnNames.length];

    
    public PlayerEntryTableModel()
    {
        for (int row = 0; row < players.length; row++)
        {
            players[row][0] = row + 1;
        }

    }
    

    /*
     * Abstract methods that must be implemented from AbstractTableModel
     */
    public String getColumnName(int col) { return columnNames[col]; }
    public int getRowCount() { return players.length; }
    public int getColumnCount() { return columnNames.length; }
    public Object getValueAt(int row, int col)
    {
        // Check for out of bounds
        if (row >= this.getRowCount()) return null;
        if (col >= this.getColumnCount()) return null;

        return players[row][col];
    }
    public boolean isCellEditable(int row, int col)
    {
        if (col == 0) return false;
        return true;
    }

    /*
     * This method is called when a cell is being edited, but before the changes happen.
     * Most code here is checking for duplicates and correct inputs.
     * May cancel the edit if an incorrect input is found.
     * When a Machine ID-Playername pair is entered, it adds it to the database.
     * When a Machine ID-Playername pair is broken, it removes it from the database.
     */
    public void setValueAt(Object value, int row, int col)
    {
        // Get old cell value
        Object oldValue = this.getValueAt(row, col);

        // Trim inputs
        value = (Object) String.valueOf(value).trim();

        // Set value to null if the new value is blank
        if (String.valueOf(value).isEmpty())
        {
            value = null;
        }

        if (Objects.equals(value, oldValue)) return; // Cancel edit (no change)

        // If the edited cell is machine ID
        // Skip this step if value is blank/null
        if (col == 1 && value != null)
        {
            // Make sure it's an integer (or whitespace/null)
            try {
                // Will throw an exception if it's not an integer
                Integer.valueOf(String.valueOf(value));
            } catch (NumberFormatException e)
            {
                System.out.println("Invalid Machine ID: Input is not a number.");
                return; // Cancel the edit
            }

            // Make sure there are no duplicate machine IDs
            if (machineIds.contains(value))
            {
                System.out.println("Invalid Machine ID: Input cannot be a duplicate.");
                return; // Cancel the edit
            }

            machineIds.remove(oldValue);
            machineIds.add(value);

        } else if (col == 2 && value != null) // If the edited cell is playername
        {                                     // Skip this step if value is blank/null
            // Make sure there are no duplicate playernames
            if (playernames.contains(value))
            {
                System.out.println("Invalid Playername: Input cannot be a duplicate.");
                return; // Cancel the edit
            }

            playernames.remove(oldValue);
            playernames.add(value);

        } else if (value == null) // User wants to delete the cell data
        {
            if (col == 1)
            {
                machineIds.remove(oldValue);
            } else if (col == 2)
            {
                playernames.remove(oldValue);
            }
        }

        // Get the machine ID and playername if they exist
        Object machineIdObject = null;
        Object playernameObject = null;
        if (col == 1 && getValueAt(row, 2) != null)
        {
            machineIdObject = value;
            playernameObject = getValueAt(row, 2);
            
        } else if (col == 2 && getValueAt(row, 1) != null)
        {
            machineIdObject = getValueAt(row, 1);
            playernameObject = value;
            
        }
        
        // If both values exist, add the new player to the database
        if (machineIdObject != null && playernameObject != null)
        {
            // Add player and machine ID to database
            DB.get().addPlayer(Integer.valueOf(String.valueOf(machineIdObject)), String.valueOf(playernameObject));
            System.out.println("Player added to database: (" + machineIdObject + ", " + playernameObject + ")");
        } else if (value == null && (machineIdObject != null || playernameObject != null))
        {   // The pair was broken, remove player from database
            Integer machineId = null;
            if (col == 1)
            {
                machineId = Integer.valueOf(String.valueOf(oldValue));
                
            } else if (col == 2)
            {
                machineId = Integer.valueOf(String.valueOf(machineIdObject));
            }

            // Remove player from database when the user deletes a player
            DB.get().removePlayer(machineId);
        }

        // Make the edit
        players[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    
}
