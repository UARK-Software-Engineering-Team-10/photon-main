package edu.uark.team10;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {

    public static void send(String message) {
        // Define the broadcast address and port
        String broadcastAddress = "127.0.0.1"; // Broadcast address
        int port = 7500; // Port to broadcast on

        try {
            // Create a DatagramSocket
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true); // Enable broadcast

            byte[] buffer = message.getBytes();

            // Create a DatagramPacket with the broadcast address and port
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(broadcastAddress), port);

            // Send the packet
            socket.send(packet);
            System.out.println("Broadcast message sent: " + message);

            // Close the socket
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Enter a message to send or type 'exit' to quit:");
        String message = scanner.nextLine();
        
        while(message != "exit") {
            send(message);
            message = scanner.nextLine();
        }
    }
}
