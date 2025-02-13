package edu.uark.team10;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient {

    public void sendPacket(int taggerMachineId, int taggedMachineId)
    {
        try {
            int port = 0; // Set port to send on
            String hostname = "192.168.1.111"; // Set address to send to
            InetAddress address = InetAddress.getByName(hostname);

            String sendString = taggerMachineId + ":" + taggedMachineId; // Send this to the server formatted as tagger:tagged
            byte[] sendData = sendString.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            DatagramSocket sendSocket = new DatagramSocket();
            sendSocket.send(sendPacket);

            System.out.println("Sent data to server (" + sendString + ") to " + InetAddress.getLocalHost().getHostAddress() + ":" + port);

            sendSocket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
