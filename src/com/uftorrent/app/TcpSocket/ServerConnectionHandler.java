package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;

import java.io.*;
import java.net.Socket;

import static java.lang.System.exit;

public class ServerConnectionHandler extends PeerProcess implements Runnable {
    private int otherPeerId;
    private Socket clientConnection;
    private PrintWriter handOut;
    private BufferedReader handIn;
    private InputStream bytesIn;
    private OutputStream bytesOut;
    public boolean isChokingTheOtherPeer;
    public boolean isNotInteresting;
    public byte[] otherPeersBitfield;
    public byte[] possiblePieces;
    public Thread connectionThread;
    private EventLogger eventLogger = new EventLogger();

    public ServerConnectionHandler(Socket clientConnection) {
        try {
            this.clientConnection = clientConnection;
            handOut = new PrintWriter(this.clientConnection.getOutputStream(), true);
            handIn = new BufferedReader(new InputStreamReader(this.clientConnection.getInputStream()));
            bytesIn = this.clientConnection.getInputStream();
            bytesOut = this.clientConnection.getOutputStream();
        } catch (Exception e) {
            System.out.println("Unable to establish a sever connection handler");
            exit(1);
        }
    }
    public void run() {
        try {
            otherPeerId = waitForHandshake();

            serverConnectionHandlers.put(otherPeerId, this);

            this.handOut.println(handshakeMessage);

            System.out.println("ServerConnectionHandler for peer " + otherPeerId);
            startListening();

        } catch (Exception e) {
            System.out.print("Whoops! ConnectionHandler " +  this.otherPeerId + " unexpectedly quit!\n" + e + "\n");
        }
    }
    private void startListening() {
        UFTorrentServerProtocol protocol = new UFTorrentServerProtocol(this.otherPeerId);
        while (true) {
            byte[] sizeHeaderFromClient = new byte[4];
            byte[] msgType = new byte[1];
            int bytesRead;
            try {
                bytesIn.read(sizeHeaderFromClient, 0, 4);
                int messageSize = util.packetSize(sizeHeaderFromClient);
                System.out.println("Size of message From Client: " + messageSize);

                bytesIn.read(msgType, 0, 1);
                System.out.println("Message type of client: " + msgType[0]);

                byte[] msgBody = new byte[messageSize - 1];
                bytesRead = bytesIn.read(msgBody, 0, msgBody.length);
                System.out.println("# of payload bytes read from client: " + bytesRead);

                bytesOut.write(protocol.handleInput(msgType[0], msgBody).msgToByteArray());
            } catch(Exception e) {
                System.out.println("Server ConnectionHandler " + this.otherPeerId + " closed\n" + e + "\n");
                exit(1);
            }

            if (sizeHeaderFromClient[0] == 'z') {
                break;
            }
        }
    }
    private int waitForHandshake() {
        try {
            String fromClient;
            while ((fromClient = handIn.readLine()) != null) {
                System.out.println("Handshake Received From Client: " + fromClient);
                if (fromClient.substring(0, 18).equals("P2PFILESHARINGPROJ")) {
                    String otherPeerId = fromClient.substring(fromClient.length() - 4);
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
}
