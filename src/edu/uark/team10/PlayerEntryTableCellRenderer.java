package edu.uark.team10;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class PlayerEntryTableCellRenderer extends DefaultTableCellRenderer
{
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
    {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cell.setFont(new Font("Serif", Font.PLAIN, 14));
        cell.setForeground(Color.WHITE);
        
        if (column == 0)
        {
            this.setHorizontalAlignment(JLabel.CENTER);
        } else
        {
            this.setHorizontalAlignment(JLabel.LEFT);
        }

        return cell;
    }
    
}
