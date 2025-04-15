package edu.uark.team10;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

public class AppKeyDispatcher implements KeyEventDispatcher {

    // Stores actions to be executed on key press
    private static LinkedList<KeyAdapter> keyActions = new LinkedList<>();

    public static void clearKeyActions()
    {
        keyActions.clear();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int eventType = e.getID();

        for (KeyAdapter action : keyActions)
        {
            // Events to trigger
            if (eventType == KeyEvent.KEY_PRESSED)
            {
                action.keyPressed(e);
            } else if (eventType == KeyEvent.KEY_RELEASED)
            {
                action.keyReleased(e);
            } else if (eventType == KeyEvent.KEY_TYPED)
            {
                action.keyTyped(e);
            }
        }
        

        return false;
    }

    public static void addKeyListener(KeyAdapter action)
    {
        keyActions.add(action); // Save the action
    }

}
