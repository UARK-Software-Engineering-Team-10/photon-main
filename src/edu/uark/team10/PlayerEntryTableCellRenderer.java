package edu.uark.team10;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This custom table cell renderer lets us modify how the cells look.
 */
public class PlayerEntryTableCellRenderer extends DefaultTableCellRenderer
{
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
        // Get the cell using the parent class
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cell.setFont(new Font("Serif", Font.PLAIN, 14)); // Change font and size
        cell.setForeground(Color.WHITE); // Change text color
        
        if (column == 0)
        {
            this.setHorizontalAlignment(JLabel.CENTER); // Center align row numbers
        } else
        {
            this.setHorizontalAlignment(JLabel.LEFT); // Left align everything else
        }

        return cell;
    }
    
}
