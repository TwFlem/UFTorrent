package com.uftorrent.app.TcpSocket;

import java.io.*;
import java.net.Socket;

public class PeerClient implements Runnable {
    private String peerId;
    private String hostName;
    private String portNumber;
    private String hasCompleteFile;
    private String handshakeMessage;
    private PrintStream out;
    private DataInputStream in;
    public PeerClient(String peerId, String hostName, String portNumber, String hasCompleteFile, String handshakeMessage) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.portNumber  = portNumber;
        this.hasCompleteFile = hasCompleteFile;
        this.handshakeMessage = handshakeMessage;
    }
    public void run() {
        try {
            System.out.println("Hello from a client thread!");

            String fromServer;

            Socket socketToPeer = new Socket(this.hostName, Integer.parseInt(this.portNumber));
            out = new PrintStream(socketToPeer.getOutputStream(), true);
            in = new DataInputStream(socketToPeer.getInputStream());

            out.println(handshakeMessage);

            while ((fromServer = in.readLine()) != null) {
                System.out.println("From Server: " + fromServer);
                out.println("Cya.");
                if (fromServer.equals("Bye.")) {
                    break;
                }

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
