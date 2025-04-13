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
        /*
         * Create a new game instance. Has helper methods and
         * member variables for managing the game.
         * Does not start the game until game.start() is called.
         */
        Game game = new Game();
        /*
         * Create a new server instance. This is used for communicating with the clients.
         * Pass the game instance to the server. The server tells the game what the players
         * are doing when it gets a packet.
         * Extends Thread-Does not start listening until server.start() is called.
         */
        UDPServer server = new UDPServer(game);

        /*
         * Create and show the user interface.
         * Pass in game and server so we can configure them and then start them.
         */
        Application application = new Application(game, server);

        // Sets the JFrame to display the gameplay screen
        // Because Game controlls the events
        game.setActionDisplay(application);

    }
    
}
