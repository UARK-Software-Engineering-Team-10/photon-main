package edu.uark.team10;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/*
 * A custom table model class for the team tables in the player entry screen
 */
public class PlayerEntryTableModel extends AbstractTableModel
{
    // Store all IDs in the table
    // Check for duplicates using this
    private static ArrayList<Object> ids = new ArrayList<>();

    // Basic table data
    private final int maxPlayers = 15;
    private String[] columnNames = new String[] {"", "ID", "Codename", "Equipment ID"};
    private Object[][] rowData = new Object[maxPlayers][columnNames.length];

    private final DB db = DB.get();
    private Game game = null;
    private UDPServer server = null;

    public PlayerEntryTableModel(Game game, UDPServer server)
    {
        this.game = game;
        this.server = server;

        // Add row numbers for looks
        for (int row = 0; row < rowData.length; row++)
        {
            rowData[row][0] = row + 1;
        }

    }

    /*
     * Abstract methods that must be implemented from AbstractTableModel
     */
    public String getColumnName(int col) { return columnNames[col]; }
    public int getRowCount() { return rowData.length; }
    public int getColumnCount() { return columnNames.length; }
    public Object getValueAt(int row, int col)
    {
        // Check for out of bounds
        if (row >= this.getRowCount()) return null;
        if (col >= this.getColumnCount()) return null;

        return rowData[row][col];
    }
    public boolean isCellEditable(int row, int col)
    {
        if (col == 0 || col == 3) return false; // Don't allow editing of the cell numbers or the hidden equipment IDs
        if (col == 2 && getValueAt(row, 1) == null) return false; // Don't allow editing of the codename if the ID isn't entered yet
        if (row != 0 && getValueAt(row - 1, 1) == null) return false; // Rows should be filled in top to bottom
        return true;
    }

    /*
     * This method is called when a cell is being edited, but before the changes happen.
     * Most code here is checking for duplicates and correct inputs.
     * May cancel the edit if an incorrect input is found.
     * When an ID-codename pair is entered, it adds it to the database.
     */
    public void setValueAt(Object value, int row, int col)
    {
        // Get old cell value
        Object oldValue = this.getValueAt(row, col);

        // Trim inputs
        value = String.valueOf(value).trim();

        // Set value to null if the new value is blank
        if (String.valueOf(value).isEmpty())
        {
            value = null;

            if (col == 1)
            {
                ids.remove(oldValue);
            }/* else if (col == 2)
            {
                codenames.remove(oldValue);
            }*/

            // Make the edit
            rowData[row][col] = value;
            fireTableCellUpdated(row, col);

            return;
        }

        if (Objects.equals(value, oldValue)) return; // Cancel edit (no change)

        // If the edited cell is ID
        if (col == 1)
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

            // Make sure there are no duplicate IDs
            if (ids.contains(value))
            {
                System.out.println("Invalid ID: Input cannot be a duplicate.");
                return; // Cancel the edit
            }

            ids.remove(oldValue);
            ids.add(value);

        } else if (col == 2) // Edited cell is codename
        {
            // Replaces any character that isn't a-0 or A-z or 0-9 or _ (only allows letters, numbers, and underscore)
            value = String.valueOf(value).replaceAll("[^\\w]", "");
        }

        // Get the ID and codename if they exist
        Integer id = null;
        String codename = null;
        if (col == 1)
        {
            id = Integer.valueOf(String.valueOf(value));
            codename = db.getCodename(id); // may be null

            if (codename != null)
            {
                rowData[row][2] = codename;
            }
            
        } else if (col == 2)
        {
            id = Integer.valueOf(String.valueOf(getValueAt(row, 1)));
            codename = String.valueOf(value);
            
        }
        
        // If both values exist, add the new player to the database.
        if (id != null && codename != null)
        {
            // Add codename and ID to database
            db.addEntry(id, codename);
            System.out.println("Player added to database: (" + id + ", " + codename + ")");

            String equipmentId = null;

            while (equipmentId == null || equipmentId.isEmpty())
            {
                equipmentId = JOptionPane.showInputDialog(null, "Enter equipment ID:", codename, JOptionPane.QUESTION_MESSAGE);
                equipmentId = equipmentId.replaceAll("[^0-9]", ""); // Only allow numbers for equipment ID

                try {
                    rowData[row][3] = Integer.valueOf(equipmentId);
                    JOptionPane.showMessageDialog(null, "Added player: " + codename, "Player Added", JOptionPane.PLAIN_MESSAGE);
                    this.server.sendMessage(equipmentId);
                } catch (NumberFormatException e) {
                    equipmentId = null;
                    JOptionPane.showMessageDialog(null, "Please try again.", "Invalid Equipment ID", JOptionPane.ERROR_MESSAGE);
                }

            }

        }

        // Make the edit
        rowData[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public ArrayList<Object[]> getRowData(int teamNumber)
    {
        ArrayList<Object[]> rowPlayerData = new ArrayList<>();

        for (int row = 0; row < this.getRowCount(); row++)
        {
            Object equipmentId = this.getValueAt(row, 3);
            Object playerId = this.getValueAt(row, 1);
            Object codename = this.getValueAt(row, 2);

            if (equipmentId == null || playerId == null || codename == null) continue;

            System.out.println("Row data: " + equipmentId + ", " + playerId + ", " + codename);

            Object[] data = new Object[] {
                equipmentId,
                playerId,
                teamNumber,
                codename
            };

            rowPlayerData.add(data);
        }

        return rowPlayerData;
    }
    
}
