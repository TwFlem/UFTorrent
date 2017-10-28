package com.uftorrent.app.TcpSocket;


import com.uftorrent.app.main.PeerProcess;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer extends PeerProcess implements Runnable{
    private PrintWriter out;
    private BufferedReader in;
    public void run() {
        System.out.println("Hello from a server thread!");
        try {
            String inputLine, outputLine;
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientConnection = serverSocket.accept();
            UFTorrentProtocol protocol = new UFTorrentProtocol("server");
            out = new PrintWriter(clientConnection.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));

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
}
