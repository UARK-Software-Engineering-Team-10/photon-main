package edu.uark.team10;

import java.util.ArrayList;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 * A custom table model used by the team tables in the player entry screen.
 * This lets us customize how the table functions when getting/adding entries and more.
 */
public class PlayerEntryTableModel extends AbstractTableModel
{
    /*
     * Static arrays for storing player IDs and equipment IDs.
     * Used for checking and disallowing duplicate IDs.
     * Shared across all PlayerEntryTableModels (both red and green team tables)
     */
    private static ArrayList<Object> playerIds = new ArrayList<>();
    private static ArrayList<Object> equipmentIds = new ArrayList<>();

    // Basic table data
    private final int maxPlayers = 15;
    private String[] columnNames = new String[] {"", "Player ID", "Codename", "Equipment ID"};
    private Object[][] rowData = new Object[maxPlayers][columnNames.length];

    // Database and server references
    private final DB db = DB.get();
    private UDPServer server = null;

    /**
     * Creates a new table model and sets the first column to have row numbers
     * 
     * @param server
     */
    public PlayerEntryTableModel(UDPServer server)
    {
        this.server = server;

        // Add row numbers to column 0 for looks
        for (int row = 0; row < rowData.length; row++)
        {
            rowData[row][0] = row + 1;
            fireTableCellUpdated(row, 0);
        }

    }

    /*
     * Abstract methods that must be implemented from AbstractTableModel
     */
    public String getColumnName(int col) { return columnNames[col]; }
    public int getRowCount() { return rowData.length; }
    public int getColumnCount() { return columnNames.length; }
    public Object getValueAt(int row, int col) { return rowData[row][col]; }
    public boolean isCellEditable(int row, int col)
    {
        if (col == 0 || col == 3) return false; // Don't allow editing of the cell numbers or the hidden equipment IDs
        if (col == 2 && getValueAt(row, 1) == null) return false; // Don't allow editing of the codename if the ID isn't entered yet
        if (row != 0 && getValueAt(row - 1, 1) == null) return false; // Rows should be filled in top to bottom (not important)
        return true;
    }

    /**
     * This method is called when a cell is being edited, but before the changes happen.
     * Most code here is checking for duplicates and correct inputs.
     * May cancel the edit if an incorrect input is found.
     * When an ID-codename pair is entered, it adds it to the database.
     * When an ID-codename pair is entered, it prompts for equipment ID.
     */
    public void setValueAt(Object value, int row, int col)
    {
        // Get old cell value
        Object oldValue = this.getValueAt(row, col);

        // Trim inputs
        value = String.valueOf(value).trim();

        // Delete the whole row if any value is deleted
        if (String.valueOf(value).isEmpty())
        {
            value = null;

            if (col == 1) // Player ID
            {
                PlayerEntryTableModel.playerIds.remove(oldValue); // Remove from duplicate checking list
            }

            // Remove equipment ID from duplicate checking list if present
            Object equipmentId = this.getValueAt(row, 3);
            if (equipmentId != null)
            {
                PlayerEntryTableModel.equipmentIds.remove(equipmentId);
            }

            // Clear the row (except for column 0--the row numbers)
            this.rowData[row][1] = null;
            this.fireTableCellUpdated(row, 1);

            this.rowData[row][2] = null;
            this.fireTableCellUpdated(row, 2);

            this.rowData[row][3] = null;
            this.fireTableCellUpdated(row, 3);

            return; // Exit method
        }

        if (Objects.equals(value, oldValue)) return; // Cancel edit if no change

        if (col == 1) // Player ID
        {
            // Make sure it's an integer
            try {
                // Will throw an exception if it's not an integer
                Integer.valueOf(String.valueOf(value));
            } catch (NumberFormatException e)
            {
                System.out.println("Invalid ID: Input is not a number.");
                return; // Cancel the edit
            }

            // Make sure there are no duplicate Player IDs
            if (PlayerEntryTableModel.playerIds.contains(value))
            {
                System.out.println("Invalid ID: Input cannot be a duplicate.");
                return; // Cancel the edit
            }

            // Remove old value from duplicate checking list
            PlayerEntryTableModel.playerIds.remove(oldValue);
            PlayerEntryTableModel.playerIds.add(value); // Add new value to dupe check list

        } else if (col == 2) // Codename
        {
            // Replaces any character that isn't a-0 or A-z or 0-9 or _ (only allows letters, numbers, and underscore)
            value = String.valueOf(value).replaceAll("[^\\w]", "");
        }

        // Get the player ID and codename if they exist
        Integer playerId = null;
        String codename = null;
        if (col == 1) // Player ID
        {
            playerId = Integer.valueOf(String.valueOf(value));
            codename = db.getCodename(playerId); // May be null if not present in database

            // Add codename to the row if not null
            if (codename != null)
            {
                this.rowData[row][2] = codename;
                this.fireTableCellUpdated(row, 2);
            }
            
        } else if (col == 2) // Codename
        {
            playerId = Integer.valueOf(String.valueOf(getValueAt(row, 1))); // Guaranteed to be present
            codename = String.valueOf(value);

            // Remove old equipment ID from duplicate checking list if it exists
            Object oldEquipmentId = this.getValueAt(row, 3);
            if (oldEquipmentId != null)
            {
                PlayerEntryTableModel.equipmentIds.remove(oldEquipmentId);
            }
            
        }
        
        /*
         * If both values exist, add the new player to the database.
         * Prompt for equipment ID.
         */
        if (playerId != null && codename != null)
        {
            // Add codename and player ID to database
            this.db.addEntry(playerId, codename); // May overwrite/update existing codename if player ID is present
            System.out.println("Player added to database: (" + playerId + ", " + codename + ")");

            // Prompt for equipment ID--loop until valid input
            String equipmentId = null;
            while (equipmentId == null || equipmentId.isEmpty())
            {
                equipmentId = JOptionPane.showInputDialog(null, "Enter equipment ID:", codename, JOptionPane.QUESTION_MESSAGE);
                equipmentId = equipmentId.replaceAll("[^0-9]", ""); // Only allow numbers for equipment ID

                try {
                    // Check that input is an integer. Throws exception if not
                    Integer equipmentIdInteger = Integer.valueOf(equipmentId);
                    // Invalid input if the equipment ID is a duplicate
                    if (PlayerEntryTableModel.equipmentIds.contains(equipmentIdInteger))
                    {
                        equipmentId = null;
                        continue; // Break and loop again
                    }

                    // Remove old equipment ID from duplicate checking list if it exists
                    Object oldEquipmentId = this.getValueAt(row, 3);
                    if (oldEquipmentId != null)
                    {
                        PlayerEntryTableModel.equipmentIds.remove(oldEquipmentId);
                    }

                    // Add equipment ID to dupe check list
                    PlayerEntryTableModel.equipmentIds.add(equipmentIdInteger);

                    // Add equipment ID to the table (this column is hidden from the user)
                    this.rowData[row][3] = equipmentIdInteger;
                    this.fireTableCellUpdated(row, 3);

                    JOptionPane.showMessageDialog(null, "Added player: " + codename, "Player Added", JOptionPane.PLAIN_MESSAGE);
                    this.server.sendMessage(equipmentId);
                } catch (NumberFormatException e) {
                    equipmentId = null;
                    JOptionPane.showMessageDialog(null, "Please try again.", "Invalid Equipment ID", JOptionPane.ERROR_MESSAGE);
                }

            }

        }

        // Make the edit
        this.rowData[row][col] = value;
        this.fireTableCellUpdated(row, col);
    }

    /**
     * Before starting a game, this method is called to extract and compile the added players
     * and related data. It does not include rows that have null values--only rows with complete data.
     * 
     * @param teamNumber The team number belonging to this table (53 for red, 43 for green)
     * @return An object array with non-null values in this order: [(int) equipment ID, (int) player ID, (int) team number, (string) codename]
     */
    public ArrayList<Object[]> getRowData(int teamNumber)
    {
        // A list to hold the row data
        ArrayList<Object[]> rowDataList = new ArrayList<>();

        // Loop through the rows
        for (int row = 0; row < this.getRowCount(); row++)
        {
            // Get the values in this row
            Object equipmentId = this.getValueAt(row, 3);
            Object playerId = this.getValueAt(row, 1);
            Object codename = this.getValueAt(row, 2);

            // Skip if any values are null
            if (equipmentId == null || playerId == null || codename == null) continue;

            System.out.println("Row data: " + equipmentId + ", " + playerId + ", " + codename);

            // Create the row data object
            Object[] data = new Object[] {
                equipmentId,
                playerId,
                teamNumber,
                codename
            };

            // Add the row data object to the list
            rowDataList.add(data);
        }

        return rowDataList;
    }

    /**
     * Completely clears the data in this table by setting the values
     * to null (excludes column 0 row numbers). Also clears the
     * duplicate checking lists.
     */
    public void clear()
    {
        for (int row = 0; row < this.getRowCount(); row++)
        {
            for (int col = 1; col < this.getColumnCount(); col++)
            {
                this.rowData[row][col] = null;
                this.fireTableCellUpdated(row, col);
            }

        }
        
        // Clear the dupe check lists
        PlayerEntryTableModel.playerIds.clear();
        PlayerEntryTableModel.equipmentIds.clear();
    }
    
}
