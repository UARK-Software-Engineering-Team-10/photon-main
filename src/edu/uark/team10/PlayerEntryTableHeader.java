package edu.uark.team10;

import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * A custom table header for setting column options
 */
public class PlayerEntryTableHeader extends JTableHeader {

    public PlayerEntryTableHeader(TableColumnModel cm)
    {
        super(cm);

        // Set width
        TableColumnModel columnModel = super.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(275);
        columnModel.removeColumn(columnModel.getColumn(3)); // Hide column 3 (equipment IDs)

        // Set the custom cell renderer
        PlayerEntryTableCellRenderer cellRenderer = new PlayerEntryTableCellRenderer();
        columnModel.getColumn(0).setCellRenderer(cellRenderer);
        columnModel.getColumn(1).setCellRenderer(cellRenderer);
        columnModel.getColumn(2).setCellRenderer(cellRenderer);

        // Create a formatted text
        NumberFormat format = DecimalFormat.getIntegerInstance(); // Integers only
        format.setMinimumIntegerDigits(1); // Min 1 digit
        format.setMaximumIntegerDigits(9); // Max 9 digits
        JFormattedTextField textField = new JFormattedTextField(format);

        // Set the custom cell editor
        PlayerEntryCellEditor cellEditor = new PlayerEntryCellEditor(textField);
        columnModel.getColumn(0).setCellEditor(cellEditor);
        columnModel.getColumn(1).setCellEditor(cellEditor);
        columnModel.getColumn(2).setCellEditor(cellEditor);

    }

    @Override
    public boolean getReorderingAllowed() { return false; }
    @Override
    public boolean getResizingAllowed() { return false; }
    @Override
    public Font getFont() { return new Font("Conthrax SemBd", Font.PLAIN, 14); } // Table header will use this font
    
}
