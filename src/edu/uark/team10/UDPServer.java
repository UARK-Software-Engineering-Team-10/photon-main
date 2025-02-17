package edu.uark.team10;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    // Game instanace
    private Game game = null;

    // Network address to bind to. Cannot be changed after the server starts
    public static String networkAddress = "127.0.0.1";

    public UDPServer(Game game) {
        this.game = game;
        
    }

    // This override method is called when start() is called
    @Override
    public void run() {
        // The socket to receive on
        DatagramSocket receiveSocket = null;

        try {
            receiveSocket = new DatagramSocket(listenPort, InetAddress.getByName(UDPServer.networkAddress));
            System.out.println("UDP Server is listening on port " + UDPServer.listenPort);

            // The buffer for the packet data
            byte[] receiveBuffer = null;

            while (true) {
                // Clear buffer for next packet
                receiveBuffer = new byte[1024]; // Should be at the top in case loop continues early

                // Receive packet
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length, InetAddress.getByName(UDPServer.networkAddress), UDPServer.listenPort);
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
                    Integer shooterTeamNumber = this.game.getTeamFromEquipmentId(shooterEquipmentId);
                    Integer targetTeamNumber = this.game.getTeamFromEquipmentId(targetEquipmentId);

                    // Player IDs
                    //Integer shooterPlayerId = this.game.getPlayerIdFromEquipmentId(shooterEquipmentId); // Unused
                    Integer targetPlayerId = this.game.getPlayerIdFromEquipmentId(targetEquipmentId);

                    // Codenames
                    String player1 = this.game.getCodename(shooterEquipmentId);
                    String player2 = null; // Check if target is a player or base

                    if (targetPlayerId != null) // Target is a player
                    {
                        player2 = this.game.getCodename(targetEquipmentId); // Set target player's codename

                        if (shooterTeamNumber != targetTeamNumber) // Tagged opposing team
                        {
                            this.game.addPoints(Game.TAG_POINTS, shooterEquipmentId);
                            message = targetEquipmentId.toString(); // Send target's equipment ID
                        } else // Tagged same team
                        {
                            this.game.addPoints(Game.TAG_POINTS * -1, shooterEquipmentId);
                            message = shooterEquipmentId.toString(); // Send shooter's equipment ID
                        }
                    } else // Target is a base
                    {
                        // If target's team number is red, the target is the red team's base
                        // else the target is the green team's base
                        player2 = targetTeamNumber == Game.RED_TEAM_NUMBER ? "Red Team's Base" : "Green Team's Base";

                        if (shooterTeamNumber != targetTeamNumber) // Tagged opposing team's base
                        {
                            this.game.addPoints(Game.BASE_POINTS, shooterEquipmentId);
                            this.game.playerScoredBase(shooterEquipmentId);

                        } // No points removed for tagging your own base
                        // Team number will be the message whether or not it's the opposite base
                        message = targetTeamNumber.toString();
                        
                    }

                    System.out.println(player1 + " Shot " + player2);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid format in received message: " + receivedMessage);
                    continue;
                }

                // Send the response message back where it came from
                this.sendMessage(message, receivePacket.getAddress());
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
    public void sendMessage(String message, InetAddress address)
    {
        try {
            // Send data to client
            byte[] sendData = message.getBytes();
            DatagramSocket sendSocket = new DatagramSocket(); // Do not bind until ready to send
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

            sendSocket.connect(address, UDPServer.sendPort); // Bind the socket and make a connection
            sendSocket.send(sendPacket);
            sendSocket.disconnect(); // Unbind
            sendSocket.close();

            System.out.println("Sent data '" + message + "' through port " + UDPServer.sendPort);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Send a message to the client using the configured network address in this class.
     * 
     * @param message The message to be sent
     */
    public void sendMessage(String message)
    {
        try {
            this.sendMessage(message, InetAddress.getByName(UDPServer.networkAddress));
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
    public void sendMessage(String message, long timeout, TimeUnit unit)
    {
        CompletableFuture<Void> futureMessage = new CompletableFuture<Void>().completeOnTimeout(null, timeout, unit);
        futureMessage.whenComplete((none, exception) -> { // Completes after timeout units
            if (exception != null)
            {
                exception.printStackTrace();
            }

            sendMessage(message);

        });

    }

}
