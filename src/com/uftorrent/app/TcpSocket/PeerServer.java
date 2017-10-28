package com.uftorrent.app.TcpSocket;


import com.uftorrent.app.main.PeerProcess;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.System.exit;

public class PeerServer extends PeerProcess implements Runnable{
    private PrintWriter out;
    private BufferedReader in;
    EventLogger eventLogger = new EventLogger();
    public void run() {
        System.out.println("Hello from a server thread!");
        try {
            String inputLine, outputLine, otherPeerId;
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientConnection = serverSocket.accept();

            out = new PrintWriter(clientConnection.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));

            otherPeerId = waitForHandshakes();
            out.println(handshakeMessage);
            UFTorrentProtocol protocol = new UFTorrentProtocol("server", otherPeerId);

            while ((inputLine = in.readLine()) != null) {
                System.out.println("From Client: " + inputLine);
                if (inputLine.equals("Cya.")) {
                    break;
                }
                outputLine = protocol.handleInput(inputLine);
                out.println(outputLine);
            }

            // Server cleanup procedure
            out.close();
            clientConnection.close();
            serverSocket.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! The Server quit unexpectedly!\n");
        }
    }

    // Wait for Clients to send handshake
    private String waitForHandshakes() {
        try {
            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                System.out.println("Handshake From Client: " + fromClient);
                if (fromClient.substring(0, 18).equals("P2PFILESHARINGPROJ")) {
                    String otherPeerId = fromClient.substring(fromClient.length() - 4);
                    eventLogger.logTCPConnectionFrom(otherPeerId);
                    return otherPeerId;
                }
            }
        }
        catch(Exception e) {
            System.out.print("Whoops! Server unexpectedly quit!\n");
            exit(1);
        }
        return "Bye.";
    }
}
