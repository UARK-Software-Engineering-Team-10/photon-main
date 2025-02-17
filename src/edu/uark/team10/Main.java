package edu.uark.team10;

public class Main {

    // Entry point
    public static void main(String[] args)
    {
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
        new Application(game, server);

    }
    
}
