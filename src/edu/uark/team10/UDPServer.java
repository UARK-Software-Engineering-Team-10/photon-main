package edu.uark.team10;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPServer extends Thread {

    private static final int listenPort = 7501; // Listen to clients on port 7501
    private static final int sendPort = 7500; // Send to clients on port 7500

    private Game game;

    public static String networkAddress = "127.0.0.1";

    public UDPServer(Game game) {
        this.game = game;
        
    }

    @Override
    public void run() {
        DatagramSocket receiveSocket = null;

        try {
            receiveSocket = new DatagramSocket();
            receiveSocket.bind(InetSocketAddress.createUnresolved(UDPServer.networkAddress, UDPServer.listenPort));
            System.out.println("UDP Server is listening on port " + UDPServer.listenPort);

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                // Receive packet
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length, InetAddress.getByName(UDPServer.networkAddress), UDPServer.listenPort);
                receiveSocket.receive(receivePacket);
                
                // Extract message and sender info
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress senderAddress = receivePacket.getAddress();

                // Print received message
                System.out.println("Received from " + senderAddress + ":" + receivePacket.getPort() + " -> " + receivedMessage);

                if (!receivedMessage.contains(":")) continue; // All packets should contain ":"

                String[] equipmentIds = receivedMessage.split(":");

                if (equipmentIds.length != 2) continue; // All packets should contain 2 ids

                String message = "";

                try {
                    Integer shooterEquipmentId = Integer.valueOf(equipmentIds[0].trim());
                    Integer targetEquipmentId = Integer.valueOf(equipmentIds[1].trim());

                    Integer shooterTeamNumber = this.game.getTeamFromEquipmentId(shooterEquipmentId);
                    Integer targetTeamNumber = this.game.getTeamFromEquipmentId(targetEquipmentId);

                    Integer shooterPlayerId = this.game.getPlayerIdFromEquipmentId(shooterEquipmentId);
                    Integer targetPlayerId = this.game.getPlayerIdFromEquipmentId(targetEquipmentId);

                    String player1 = this.game.getCodename(shooterEquipmentId);
                    String player2 = null; // Check if target is a player or base

                    if (targetPlayerId != null) // Target is a player
                    {
                        player2 = this.game.getCodename(targetEquipmentId);

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
                        // if target's team number is red, the target is the red team's base
                        // else the target is the green team's base
                        player2 = targetTeamNumber == Game.RED_TEAM_NUMBER ? "Red Team's Base" : "Green Team's Base";

                        if (shooterTeamNumber != targetTeamNumber) // Tagged opposing team's base
                        {
                            this.game.addPoints(Game.BASE_POINTS, shooterEquipmentId);
                            this.game.playerScoredBase(shooterEquipmentId);
                        } // No points removed for tagging your own base

                    }

                    System.out.println(player1 + " Shot " + player2);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid format in received message: " + receivedMessage);
                    continue;
                }

                this.sendMessage(message, senderAddress);

                // Clear buffer for next packet
                receiveBuffer = new byte[1024];
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            receiveSocket.close();
        }

    }

    public void sendMessage(String message)
    {
        try {
            InetAddress address = InetAddress.getByName(UDPServer.networkAddress);
            // Send data to client
            byte[] sendData = message.getBytes();
            DatagramSocket sendSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

            sendSocket.connect(address, UDPServer.sendPort);
            sendSocket.send(sendPacket);
            sendSocket.disconnect();
            sendSocket.close();

            System.out.println("Sent data '" + message + "' through port " + UDPServer.sendPort);

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void sendMessage(String message, InetAddress address)
    {
        try {
            // Send data to client
            byte[] sendData = message.getBytes();
            DatagramSocket sendSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

            sendSocket.connect(address, UDPServer.sendPort);
            sendSocket.send(sendPacket);
            sendSocket.disconnect();
            sendSocket.close();

            System.out.println("Sent data '" + message + "' through port " + UDPServer.sendPort);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
