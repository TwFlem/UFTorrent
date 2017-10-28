package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;

import java.io.*;
import java.net.Socket;

public class PeerClient extends PeerProcess implements Runnable {
    private PrintStream out;
    private DataInputStream in;
    public void run() {
        try {
            System.out.println("Hello from a client thread!");

            String fromServer;

            Socket socketToPeer = new Socket(hostName, portNumber);
            UFTorrentProtocol protocol = new UFTorrentProtocol("client");
            out = new PrintStream(socketToPeer.getOutputStream(), true);
            in = new DataInputStream(socketToPeer.getInputStream());

            out.println(handshakeMessage);

            while ((fromServer = in.readLine()) != null) {
                System.out.println("From Server: " + fromServer);
                if (fromServer.equals("Bye.")
                        || fromServer.equals("Send me a handshake first, then let's talk.")) {
                    break;
                }
                protocol.handleInput(fromServer);
                out.println("Cya.");
            }

            // Wait until the byte stream finishes reading bytes

            // Clean up
            in.close();
            out.close();
            socketToPeer.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! Client unexpectedly quit!\n");
        }
    }

    // Testing method for simulating log output
    public void simulateLogs() {
        EventLogger logger = new EventLogger();
        String[] prefferedNeighbors = {"1000", "1001", "1002"};
        logger.logTCPConnectionTo("9999");
        logger.logTCPConnectionFrom("9999");
        logger.changePreferedNeighbor(prefferedNeighbors);
        logger.optimisticallyUnchockedNeighbor("9999");
        logger.unchokedNeighbor("9999");
        logger.chokeNeighbor("9999");
        logger.receivedHaveMsg("9999", "some piece index");
        logger.receiveInteresedMsg("9999");
        logger.receiveNotInterestedMsg("9999");
        logger.downloadedPiece("9999", "some index", 420);
        logger.downloadComplete("5000");
    }
}
