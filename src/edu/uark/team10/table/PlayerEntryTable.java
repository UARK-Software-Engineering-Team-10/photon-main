package edu.uark.team10.table;

import java.awt.Color;

import javax.swing.JTable;

import edu.uark.team10.UDPServer;

/**
 * A custom JTable class for the player entry screen
 */
public class PlayerEntryTable extends JTable {

    public PlayerEntryTable(UDPServer server, Color teamColor)
    {
        this.setModel(new PlayerEntryTableModel(server));
        this.setTableHeader(new PlayerEntryTableHeader(this.getColumnModel()));
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setRowSelectionAllowed(false);
        this.setColumnSelectionAllowed(false);
        this.setBackground(teamColor);

    }

    public PlayerEntryTableModel getPlayerEntryTableModel()
    {
        return (PlayerEntryTableModel) this.getModel();
    }

    public void clear()
    {
        this.getPlayerEntryTableModel().clear();
    }

}