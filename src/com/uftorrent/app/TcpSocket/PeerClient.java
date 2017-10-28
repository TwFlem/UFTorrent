package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;

import java.io.*;
import java.net.Socket;

import static java.lang.System.exit;

public class PeerClient extends PeerProcess implements Runnable {
    private PrintStream out;
    private DataInputStream in;
    private EventLogger eventLogger = new EventLogger();
    public void run() {
        try {
            System.out.println("Hello from a client thread!");

            String fromServer, otherPeerId;


            Socket socketToPeer = new Socket(hostName, portNumber);
            out = new PrintStream(socketToPeer.getOutputStream(), true);
            in = new DataInputStream(socketToPeer.getInputStream());

            out.println(handshakeMessage);
            otherPeerId = waitForHandshake();
            UFTorrentProtocol protocol = new UFTorrentProtocol("client", otherPeerId);


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

    // Wait for server to send back handshake
    private String waitForHandshake() {
        try {
            String fromServer;
            while ((fromServer = in.readLine()) != null) {
                System.out.println("Handshake From Server: " + fromServer);
                if (fromServer.substring(0, 18).equals("P2PFILESHARINGPROJ")) {
                    String otherPeerId = fromServer.substring(fromServer.length() - 4);
                    eventLogger.logTCPConnectionTo(otherPeerId);
                    return otherPeerId;
                }
            }
        }
        catch(Exception e) {
            System.out.print("Whoops! Client unexpectedly quit!\n");
            exit(1);
        }
        return "Cya.";
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
