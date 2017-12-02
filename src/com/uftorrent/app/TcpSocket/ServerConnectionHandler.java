package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ServerConnectionHandler extends PeerProcess implements Runnable {
    private int otherPeerId;
    private Socket clientConnection;
    private PrintWriter handOut;
    private BufferedReader handIn;
    private InputStream bytesIn;
    private OutputStream bytesOut;

    public boolean isPreferred;
    public boolean isChokingTheOtherPeer;
    public double downloadRate;
    public boolean isNotInteresting;

    public boolean isChokingClient;
    public boolean isInterestedInMe;
    public boolean noLongerNeedsToServe;

    public byte[] otherPeersBitfield;
    public byte[] possiblePieces;
    public Thread connectionThread;
    private EventLogger eventLogger = new EventLogger();

    public ServerConnectionHandler(Socket clientConnection) {
        try {
            this.clientConnection = clientConnection;
            this.isChokingClient = true;
            handOut = new PrintWriter(this.clientConnection.getOutputStream(), true);
            handIn = new BufferedReader(new InputStreamReader(this.clientConnection.getInputStream()));
            bytesIn = this.clientConnection.getInputStream();
            bytesOut = this.clientConnection.getOutputStream();
            this.noLongerNeedsToServe = false;
        } catch (Exception e) {
            System.out.println("Unable to establish a sever connection handler");
        }
    }
    public void run() {
        try {
            System.out.println("Server connection handler " + peerId + " started");
            this.otherPeerId = waitForHandshake();
            serverConnectionHandlers.put(this.otherPeerId, this);
            System.out.println("ServerConnectionHandler for peer " + otherPeerId);
            this.handOut.println(handshakeMessage);

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
                int messageSize = util.byteArrayToInt(sizeHeaderFromClient);
                System.out.println("Size of message From Client " + this.otherPeerId + ": " + messageSize);

                util.sleep(1);
                if (messageSize == 0) {
                    System.out.println("serverConnectionHandler " + peerId + " Waiting on " + this.otherPeerId);
                    continue;
                }

                if (Arrays.equals(fullBitfield, this.otherPeersBitfield)) {
                    clientConnection.close();
                    System.out.println(this.otherPeerId + " client has complete file, " + peerId + " sever thread is ending");
                    break;
                }

                bytesIn.read(msgType, 0, 1);
                System.out.println("Message type of client: " + msgType[0]);

                byte[] msgBody = new byte[messageSize - 1];
                bytesRead = bytesIn.read(msgBody, 0, msgBody.length);
                System.out.println("# of payload bytes read from client: " + bytesRead);
                byte[] msg = protocol.handleInput(msgType[0], msgBody).msgToByteArray();
                util.printMsg(msg, peerId, this.otherPeerId, "server", "client");
                if (msgType[0] == 0x8) {
                    System.out.println("blank message");
                } else {
                    this.bytesOut.write(msg);
                }
            } catch(Exception e) {
                System.out.println("Server ConnectionHandler " + this.otherPeerId + " closed\n" + e + "\n");
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
                    eventLogger.logTCPConnectionFrom(Integer.parseInt(otherPeerId));
                    return Integer.parseInt(otherPeerId);
                }
            }
        }
        catch(Exception e) {
            System.out.print("Whoops! Server unexpectedly quit!\n" + e.getMessage());
        }
        return 0;
    }

    public void choke() {
        isChokingTheOtherPeer = true;
    }
    public void unchoke() {
        isChokingTheOtherPeer = false;
    }
    public void sendHaveMessage(int pieceIndex)
    {
        try {
            Message haveMessage = new Message((byte) 0x5, (byte) 0x4, util.intToByteArray(pieceIndex));
            System.out.println("Sending a have message of length: " + haveMessage.getLength());
            byte[] msg2 = haveMessage.msgToByteArray();
            util.printMsg(msg2, peerId, this.otherPeerId, "server", "client");
            this.bytesOut.write(msg2);
        }
        catch (Exception e) {
            System.out.println("Problem sending a have message " + this.otherPeerId + "\n" + e + "\n");
        }

    }
}
