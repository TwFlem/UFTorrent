package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

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
    public byte[] possiblePieces; //The bitfield representing pieces I don't have that the other peer does
    public byte[] otherPeersBitfield;
    private EventLogger eventLogger = new EventLogger();
    public ClientConnectionHandler(String hostName, int port) {
        Exception cantConnect = new Exception();
        while (cantConnect != null)
            try {
                util.sleep(1);
                this.socketToPeer = new Socket(hostName, port);
                this.isInterested = false;
                this.handOut = new PrintStream(socketToPeer.getOutputStream(), true);
                this.handIn = new DataInputStream(socketToPeer.getInputStream());
                this.bytesIn = socketToPeer.getInputStream();
                this.bytesOut = socketToPeer.getOutputStream();
                cantConnect = null;
            } catch(Exception e) {
                cantConnect = e;
                System.out.println("Unable to connect to " + hostName + " at " + port);
            }
    }
    public void run() {
        try {
            System.out.println("Client connection handler " + peerId + " started");
            this.handOut.println(handshakeMessage);
            this.otherPeerId = waitForHandshake();
            clientConnectionHandlers.put(otherPeerId, this);
            System.out.println("ClientConnectionHandler for " + this.otherPeerId);

            Message initialBitfieldMessage = new Message(bitfield.length + 1, (byte)5, bitfield);
            this.bytesOut.write(initialBitfieldMessage.msgToByteArray());

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
            while ((fromServer = this.handIn.readLine()) != null) {
                System.out.println("Handshake Received From Server: " + fromServer);
                if (fromServer.substring(0, 18).equals("P2PFILESHARINGPROJ")) {
                    String otherPeerId = fromServer.substring(fromServer.length() - 4);
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
    private void startAsking() {
        UFTorrentClientProtocol protocol = new UFTorrentClientProtocol(this.otherPeerId);
        while (true) {
            byte[] sizeHeaderFromServer = new byte[4];
            byte[] msgType = new byte[1];
            int bytesRead;

            try {
                this.bytesIn.read(sizeHeaderFromServer, 0, 4);
                int messageSize = util.byteArrayToInt(sizeHeaderFromServer);
                System.out.println("Size of message From Server: " + messageSize);

                util.sleep(1);
                if (messageSize == 0) {
                    System.out.println("clientConnectionHandler " + peerId + " Waiting on " + this.otherPeerId);
                    continue;
                }

                if (peerInfo.getHasCompleteFile(peerId)) {
                    socketToPeer.close();
                    System.out.println(peerId + " client has complete file, closing connection to " + this.otherPeerId + " server");
                    break;
                }

                bytesIn.read(msgType, 0, 1);
                System.out.println("Message type of server: " + msgType[0]);

                byte[] msgBody = new byte[messageSize - 1];
                bytesRead = bytesIn.read(msgBody, 0, msgBody.length);
                System.out.println("# of payload bytes read from server: " + bytesRead);

                byte[] msg = protocol.handleInput(msgType[0], msgBody).msgToByteArray();
                util.printMsg(msg, peerId, this.otherPeerId, "client", "server");
                if (msgType[0] == 0x8) {
                    System.out.println("blank message");
                }
                this.bytesOut.write(msg);
            } catch (Exception e) {
                System.out.println("Problem Reading from Server " + this.otherPeerId + "\n" + e + "\n");
            }

        }

    }
    public void sendHaveMessage(int pieceIndex)
    {
        try {
            Message haveMessage = new Message((byte) 0x5, (byte) 0x4, util.intToByteArray(pieceIndex));
            System.out.println("Sending a have message of length: " + haveMessage.getLength());
            byte[] msg2 = haveMessage.msgToByteArray();
            util.printMsg(msg2, peerId, this.otherPeerId, "client", "server");
            this.bytesOut.write(msg2);
        }
        catch (Exception e) {
            System.out.println("Problem sending a have message " + this.otherPeerId + "\n" + e + "\n");
        }

    }
}
