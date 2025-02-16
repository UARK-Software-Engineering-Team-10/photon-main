package edu.uark.team10;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import edu.uark.team10.DB;

public class UDPServer {

    private static final int port = 7500; // Listen on port 7500 (to match UDPClient.java)
    private DB db;

    public  UDPServer() {
        this.db = DB.get();
        
    }

    public void startServer() {
        try {
            // Open a new terminal window to display incoming messages
            //openTerminalWindow();

            DatagramSocket serverSocket = new DatagramSocket(port);
            System.out.println("UDP Server is listening on port " + port);

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                // Receive packet
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);
                
                // Extract message and sender info
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress senderAddress = receivePacket.getAddress();
                int senderPort = receivePacket.getPort();

                // Print received message
                System.out.println("Received from " + senderAddress + ":" + senderPort + " -> " + receivedMessage);

                // If message is "bye", terminate server
                if (receivedMessage.equalsIgnoreCase("bye")) {
                    System.out.println("Client sent 'bye'... Server exiting.");
                    break;
                }

                if (receivedMessage.contains(":")) {
                    String[] ids = receivedMessage.split(":");

                    if (ids.length == 2) {
                        try {
                            int shooterId = Integer.parseInt(ids[0].trim());
                            int targetId = Integer.parseInt(ids[1].trim());

                            String player1 = db.getPlayername(shooterId);
                            String player2 = db.getPlayername(targetId);

                            System.out.println(player1 + " Shot " + player2);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid format in received message: " + receivedMessage);
                        }
                    }
                }

                // Respond to client
                String responseMessage = "Received: " + receivedMessage;
                byte[] responseData = responseMessage.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, senderAddress, senderPort);
                serverSocket.send(responsePacket);

                // Clear buffer for next packet
                receiveBuffer = new byte[1024];
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        //DBServer();

        server.startServer();
    }
}
