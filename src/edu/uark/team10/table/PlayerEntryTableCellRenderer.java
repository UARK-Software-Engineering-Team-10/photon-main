package edu.uark.team10.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * This custom table cell renderer lets us modify how the cells look.
 */
public class PlayerEntryTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
        // Get the cell using the parent class
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cell.setFont(new Font("Conthrax SemBd", Font.PLAIN, 12)); // Change font and size
        
        // Change text color when selected
        if (hasFocus && table.isCellEditable(row, column))
        {
            cell.setForeground(Color.BLACK);
        } else
        {
            cell.setForeground(Color.WHITE);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Conthrax SemBd", Font.PLAIN, 14));
        
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
