package edu.uark.team10.table;

import java.awt.Font;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

/**
 * A custom cell editor to change the font when editing a cell
 */
public class PlayerEntryCellEditor extends DefaultCellEditor {

    public PlayerEntryCellEditor(JTextField textField) {
        super(textField);
        textField.setFont(new Font("Conthrax SemBd", Font.PLAIN, 12)); // Change font
    }
    
}
