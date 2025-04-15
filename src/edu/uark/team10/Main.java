package edu.uark.team10;

import java.awt.KeyboardFocusManager;

public class Main {

    // Entry point
    public static void main(String[] args)
    {
       /*
         * Create an instance of the KeyboardFocusManager and add a new KeyEventDispatcher to it.
         * This helps the application detect any keyboard inputs from the user regardless of the focus.
         */
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new AppKeyDispatcher());
        
        Application.getInstance();

    }
    
}
