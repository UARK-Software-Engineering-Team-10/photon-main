package edu.uark.team10;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Runs a server asynchronously to communicate with the client.
 * Receives packets from the client. The server is responsible for
 * managing the game. It uses a Game reference and calls corresponding methods.
 */
public class UDPServer extends Thread {

    private static final int listenPort = 7501; // Listen to clients on port 7501
    private static final int sendPort = 7500; // Send to clients on port 7500

    private static UDPServer singleton = null;

    // The socket to receive on
    private DatagramSocket receiveSocket = null;
    // Network address to bind to. Cannot be changed after the server starts
    private String networkAddress = "127.0.0.1";
    // Game instanace
    private Game game = null;
    private boolean run = true;

    public static UDPServer getInstance()
    {
        if (singleton == null)
        {
            singleton = new UDPServer();
        }

        return singleton;
    }

    public static void setGame(Game newGame)
    {
        getInstance().game = newGame;
    }

    public static void stopServer()
    {
        getInstance().run = false;
        getInstance().interrupt();
        getInstance().receiveSocket.close();
        
        singleton = new UDPServer();
    }

    public static void setAddress(String address)
    {
        getInstance().networkAddress = address;
    }

    public static String getAddress()
    {
        return getInstance().networkAddress;
    }

    // This override method is called when start() is called
    @Override
    public void run() {

        try {
            receiveSocket = new DatagramSocket(listenPort, InetAddress.getByName(networkAddress));
            System.out.println("UDP Server is listening on port " + listenPort);

            // The buffer for the packet data
            byte[] receiveBuffer = null;

            while (run) {
                // Clear buffer for next packet
                receiveBuffer = new byte[1024]; // Should be at the top in case loop continues early

                // Receive packet
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length, InetAddress.getByName(networkAddress), listenPort);
                receiveSocket.receive(receivePacket); // Blocks until a packet is received
                
                // Extract message and sender info
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Print received message
                System.out.println("Received from " + receivePacket.getAddress() + ":" + receivePacket.getPort() + " -> " + receivedMessage);

                if (!receivedMessage.contains(":")) continue; // All packets should contain ":"

                String[] equipmentIds = receivedMessage.split(":");
                if (equipmentIds.length != 2) continue; // All packets should contain 2 ids

                String message = ""; // Build the message to send back to the client

                try {
                    // Equipment IDs
                    Integer shooterEquipmentId = Integer.valueOf(equipmentIds[0].trim());
                    Integer targetEquipmentId = Integer.valueOf(equipmentIds[1].trim());

                    // Team numbers
                    Integer shooterTeamNumber = game.getTeamFromEquipmentId(shooterEquipmentId);
                    Integer targetTeamNumber = game.getTeamFromEquipmentId(targetEquipmentId);

                    // Player ID
                    Integer targetPlayerId = game.getPlayerIdFromEquipmentId(targetEquipmentId);

                    // Codenames
                    String player1 = game.getCodename(shooterEquipmentId);
                    String player2 = null; // Check if target is a player or base

                    if (targetPlayerId != null) // Target is a player
                    {
                        player2 = game.getCodename(targetEquipmentId); // Set target player's codename

                        if (shooterTeamNumber != targetTeamNumber) // Tagged opposing team
                        {
                            game.addPoints(Game.TAG_POINTS, shooterEquipmentId);
                            message = targetEquipmentId.toString(); // Send target's equipment ID
                        } else // Tagged same team
                        {
                            game.addPoints(Game.TAG_POINTS * -1, shooterEquipmentId);
                            message = shooterEquipmentId.toString(); // Send shooter's equipment ID
                        }
                    } else // Target is a base
                    {
                        // If target's team number is red, the target is the red team's base
                        // else the target is the green team's base
                        player2 = targetTeamNumber == Game.RED_TEAM_NUMBER ? "Red Team's Base" : "Green Team's Base";

                        if (shooterTeamNumber != targetTeamNumber) // Tagged opposing team's base
                        {
                            game.addPoints(Game.BASE_POINTS, shooterEquipmentId);
                            game.setPlayerScoredBase(shooterEquipmentId);

                        } // No points removed for tagging your own base
                        // Team number will be the message whether or not it's the opposite base
                        message = targetTeamNumber.toString();
                        
                    }

                    double secondsElapsed = (double) (Instant.now().getEpochSecond() - game.startInstant.getEpochSecond());
                    double minutesElapsed = secondsElapsed / 60.0;
                    String timeElapsed = String.format("%02.0f:%02.0f", Math.floor(minutesElapsed), (minutesElapsed - Math.floor(minutesElapsed)) * 60);
                    String logMessage = "[" + timeElapsed + "] " + player1 + " HIT " + player2;

                    System.out.println(logMessage);
                    game.updateActionDisplay(logMessage);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid format in received message: " + receivedMessage);
                    continue;
                }

                // Send the response message back where it came from
                UDPServer.sendMessage(message, receivePacket.getAddress());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            receiveSocket.close();
        }

    }

    /**
     * Send a message to the client using the supplied network address.
     * 
     * @param message The message to be sent
     * @param address The address to send the packet through
     */
    public static void sendMessage(String message, InetAddress address)
    {
        try {
            // Send data to client
            byte[] sendData = message.getBytes();
            DatagramSocket sendSocket = new DatagramSocket(); // Do not bind until ready to send
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

            sendSocket.connect(address, sendPort); // Bind the socket and make a connection
            sendSocket.send(sendPacket);
            sendSocket.disconnect(); // Unbind
            sendSocket.close();

            System.out.println("Sent data '" + message + "' through port " + sendPort);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Send a message to the client using the configured network address in this class.
     * 
     * @param message The message to be sent
     */
    public static void sendMessage(String message)
    {
        try {
            UDPServer.sendMessage(message, InetAddress.getByName(getInstance().networkAddress));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a message to the client using the configured network
     * address in this class after the given timeout (useful for delayed sending).
     * 
     * @param message The message to be sent
     * @param timeout The amount of time to wait before sending
     * @param unit The time unit to use
     */
    public static void sendMessage(String message, long timeout, TimeUnit unit)
    {
        CompletableFuture<Void> futureMessage = new CompletableFuture<Void>().completeOnTimeout(null, timeout, unit);
        futureMessage.whenComplete((none, exception) -> { // Completes after timeout units
            if (exception != null)
            {
                exception.printStackTrace();
            }

            UDPServer.sendMessage(message);

        });

    }

}
