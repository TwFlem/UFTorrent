package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;

import java.io.*;
import java.net.Socket;

import static java.lang.System.exit;

public class PeerClient extends PeerProcess implements Runnable {
    private PrintStream handOut;
    private DataInputStream handIn;
    private InputStream bytesIn;
    private OutputStream bytesOut;
    private EventLogger eventLogger = new EventLogger();
    public void run() {
        try {
            System.out.println("Hello from a client thread!");

            String otherPeerId;


            Socket socketToPeer = new Socket(hostName, portNumber);
            handOut = new PrintStream(socketToPeer.getOutputStream(), true);
            handIn = new DataInputStream(socketToPeer.getInputStream());

            handOut.println(handshakeMessage);
            otherPeerId = waitForHandshake();


            bytesIn = socketToPeer.getInputStream();
            bytesOut = socketToPeer.getOutputStream();

            UFTorrentClientProtocol protocol = new UFTorrentClientProtocol();

            Message initialBitfieldMessage = new Message(bitfield.length + 1, (byte)5, bitfield);
            bytesOut.write(initialBitfieldMessage.msgToByteArray());


            while (true) {
                byte[] sizeHeaderFromClient = new byte[4];
                byte[] msgType = new byte[1];
                int bytesRead;

                bytesIn.read(sizeHeaderFromClient, 0, 4);
                int messageSize = util.packetSize(sizeHeaderFromClient);
                System.out.println("Size of message From Client: " + messageSize);

                bytesIn.read(msgType, 0, 1);
                System.out.println("Message type of client: " + msgType[0]);

                byte[] msgBody = new byte[messageSize - 1];
                bytesRead = bytesIn.read(msgBody, 0, msgBody.length);
                System.out.println("# of payload bytes read from client: " + bytesRead);

                bytesOut.write(protocol.handleInput(msgType[0], msgBody).msgToByteArray());

                if (msgType[0] == 0x05) {
                    break;
                }
            }

            // Wait until the byte stream finishes reading bytes

            // Clean up
            socketToPeer.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! Client unexpectedly quit!\n" + e + "\n");
        }
    }

    // Wait for server to send back handshake
    private String waitForHandshake() {
        try {
            String fromServer;
            while ((fromServer = handIn.readLine()) != null) {
                System.out.println("Handshake Read From Server: " + fromServer);
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
