package com.uftorrent.app.TcpSocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class PeerClient implements Runnable {
    public PeerClient() {
    }
    public void run() {
        try {
            System.out.println("Hello from a client thread!");
            Socket skt = new Socket("localhost", 1234);
            BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));

            // Wait until the byte stream finishes reading bytes
            while (!in.ready()) {}
            System.out.println("Received string: " + in.readLine());

            // Close reader
            in.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! Client unexpectedly quit!\n");
        }
    }
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
