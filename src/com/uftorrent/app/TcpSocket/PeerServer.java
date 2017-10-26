package com.uftorrent.app.TcpSocket;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer implements Runnable {
    private String peerId;
    private String hostName;
    private String portNumber;
    private String hasCompleteFile;
    private String handshakeMessage;
    private PrintWriter out;
    private BufferedReader in;
    public PeerServer(String peerId, String hostName, String portNumber, String hasCompleteFile, String handshakeMessage) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.portNumber  = portNumber;
        this.hasCompleteFile = hasCompleteFile;
        this.handshakeMessage = handshakeMessage;
    }
    public void run() {
        System.out.println("Hello from a server thread!");
        String genericData = "Thanks for the message!";
        try {
            String inputLine, outputLine;
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(portNumber));
            Socket clientConnection = serverSocket.accept();
            out = new PrintWriter(clientConnection.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));

            while ((inputLine = in.readLine()) != null) {
                outputLine = genericData;
                out.println(outputLine);
                if (outputLine.equals(genericData))
                    break;
            }

            // Server cleanup procedure
            serverOut.close();
            clientConnection.close();
            serverSocket.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! The Server quit unexpectedly!\n");
        }
    }
}
