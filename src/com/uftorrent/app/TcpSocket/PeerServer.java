package com.uftorrent.app.TcpSocket;


import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.System.exit;

public class PeerServer extends PeerProcess implements Runnable{
    private PrintWriter handOut;
    private BufferedReader handIn;
    private InputStream bytesIn;
    private OutputStream bytesOut;
    EventLogger eventLogger = new EventLogger();
    public void run() {
        System.out.println("Hello from a server thread!");
        try {
            String otherPeerId;
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientConnection = serverSocket.accept();

            handOut = new PrintWriter(clientConnection.getOutputStream(), true);
            handIn = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));

            otherPeerId = waitForHandshakes();
            handOut.println(handshakeMessage);

            bytesIn = clientConnection.getInputStream();
            bytesOut = clientConnection.getOutputStream();
            UFTorrentServerProtocol protocol = new UFTorrentServerProtocol("server", otherPeerId);

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

                if (sizeHeaderFromClient[0] == 'z') {
                    break;
                }
//                handOut.println(protocol.handleInput(inputLine));
            }

            // Server cleanup procedure
            clientConnection.close();
            serverSocket.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! The Server quit unexpectedly!\n" + e + "\n");
        }
    }

    // Wait for Clients to send handshake
    private String waitForHandshakes() {
        try {
            String fromClient;
            while ((fromClient = handIn.readLine()) != null) {
                System.out.println("Handshake Received From Client: " + fromClient);
                if (fromClient.substring(0, 18).equals("P2PFILESHARINGPROJ")) {
                    String otherPeerId = fromClient.substring(fromClient.length() - 4);
                    eventLogger.logTCPConnectionFrom(otherPeerId);
                    return otherPeerId;
                }
            }
        }
        catch(Exception e) {
            System.out.print("Whoops! Server unexpectedly quit!\n" + e.getMessage());
            exit(1);
        }
        return "Bye.";
    }
}
