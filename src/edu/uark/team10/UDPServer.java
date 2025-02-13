package edu.uark.team10;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

    public void run()
    {
        int port = 0; // Set port to listen on
        DatagramSocket serverSocket = null;

        try {
            serverSocket = new DatagramSocket(port);
            byte[] receiveData = new byte[8];

            System.out.print("Listening on udp: " + InetAddress.getLocalHost().getHostAddress() + ":" + port);     
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while(true)
            {
                serverSocket.receive(receivePacket);

                // A player was tagged. This is the data formatted as "tagger:tagged"
                String receiveString = new String( receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Received data: " + receiveString);
                
                String taggedPlayerMachineId = receiveString.substring(receiveString.indexOf(":"));

                String sendString = taggedPlayerMachineId; // This gets sent to the tagged player
                byte[] sendData = sendString.getBytes("UTF-8");

                // now send acknowledgement packet back to sender     
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                                                receivePacket.getAddress(), receivePacket.getPort());
                serverSocket.send(sendPacket);

                System.out.println("Sending data to client: " + sendString);
            }
        } catch (IOException e) {
                System.out.println(e);
        } finally {
            serverSocket.close();
        }

    }
    
}
