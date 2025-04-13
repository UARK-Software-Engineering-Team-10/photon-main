package edu.uark.team10;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;

import javax.swing.JComponent;

public class AppKeyDispatcher implements KeyEventDispatcher {

    // Stores actions to be executed on key press
    // <action, action's parent>
    private static LinkedHashMap<KeyAdapter, JComponent> keyActions = new LinkedHashMap<>();

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int eventType = e.getID();

        for (KeyAdapter action : keyActions.keySet())
        {
            if (!keyActions.get(action).isShowing()) // The action's parent must be visible on the screen to be valid
            {
                // This happens when the parent component is removed from the screen
                System.out.println("AppKeyDispatcher: Invalid parent found");
                keyActions.remove(action);
                continue;
            }

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

    public static void addKeyListener(KeyAdapter action, JComponent parent)
    {
        try {
            if (parent == null)
            {
                throw new Exception("Invalid parent component: the parent component is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        keyActions.put(action, parent); // Save the action
    }

}
