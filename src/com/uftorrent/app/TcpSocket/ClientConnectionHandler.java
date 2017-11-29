package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import static java.lang.System.exit;

public class ClientConnectionHandler extends PeerProcess implements Runnable {
    private int otherPeerId;
    public boolean isInterested;
    private Socket socketToPeer;
    private PrintStream handOut;
    private DataInputStream handIn;
    private InputStream bytesIn;
    private OutputStream bytesOut;
    public boolean isInterestedInOtherPeer;
    public boolean isChoked;
    public Thread connectionThread;
    private byte[] possiblePieces; //The bitfield representing pieces I don't have that the other peer does
    private byte[] otherPeersBitfield;
    private EventLogger eventLogger = new EventLogger();
    public ClientConnectionHandler(String hostName, int port) {
        try {
            this.socketToPeer = new Socket(hostName, port);
            this.isInterested = false;
            handOut = new PrintStream(socketToPeer.getOutputStream(), true);
            handIn = new DataInputStream(socketToPeer.getInputStream());
            bytesIn = socketToPeer.getInputStream();
            bytesOut = socketToPeer.getOutputStream();
        } catch(Exception e) {
            System.out.println("Unable to connect to " + hostName + " at " + port);
        }
    }
    public void run() {
        try {

            handOut.println(handshakeMessage);
            this.otherPeerId = waitForHandshake();

            clientConnectionHandlers.put(otherPeerId, this);
            System.out.println("ClientConnectionHandler for " + this.otherPeerId);

            Message initialBitfieldMessage = new Message(bitfield.length + 1, (byte)5, bitfield);
            bytesOut.write(initialBitfieldMessage.msgToByteArray());

            this.startAsking();

            socketToPeer.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! Client unexpectedly quit!\n" + e + "\n");
        }
    }
    private int waitForHandshake() {
        try {
            String fromServer;
            while ((fromServer = handIn.readLine()) != null) {
                System.out.println("Handshake Received From Server: " + fromServer);
                if (fromServer.substring(0, 18).equals("P2PFILESHARINGPROJ")) {
                    String otherPeerId = fromServer.substring(fromServer.length() - 4);
                    eventLogger.logTCPConnectionFrom(otherPeerId);
                    return Integer.parseInt(otherPeerId);
                }
            }
        }
        catch(Exception e) {
            System.out.print("Whoops! Server unexpectedly quit!\n" + e.getMessage());
            exit(1);
        }
        return 0;
    }
    private void startAsking() {
        UFTorrentClientProtocol protocol = new UFTorrentClientProtocol(this.otherPeerId);
        while (true) {
            byte[] sizeHeaderFromServer = new byte[4];
            byte[] msgType = new byte[1];
            int bytesRead;

            try {
                bytesIn.read(sizeHeaderFromServer, 0, 4);
                int messageSize = util.packetSize(sizeHeaderFromServer);
                System.out.println("Size of message From Server: " + messageSize);

                bytesIn.read(msgType, 0, 1);
                System.out.println("Message type of server: " + msgType[0]);

                byte[] msgBody = new byte[messageSize - 1];
                bytesRead = bytesIn.read(msgBody, 0, msgBody.length);
                System.out.println("# of payload bytes read from server: " + bytesRead);

                bytesOut.write(protocol.handleInput(msgType[0], msgBody).msgToByteArray());
            } catch (Exception e) {
                System.out.println("Problem Reading from Server " + this.otherPeerId + "\n" + e + "\n");
            }

            if (msgType[0] == 0x05) {
                break;
            }
        }

    }
}
