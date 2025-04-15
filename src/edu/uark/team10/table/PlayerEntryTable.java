package edu.uark.team10.table;

import java.awt.Color;

import javax.swing.JTable;

/**
 * A custom JTable class for the player entry screen
 */
public class PlayerEntryTable extends JTable {

    public PlayerEntryTable(Color teamColor)
    {
        this.setModel(new PlayerEntryTableModel());
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