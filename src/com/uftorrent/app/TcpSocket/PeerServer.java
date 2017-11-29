package com.uftorrent.app.TcpSocket;


import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.System.exit;

public class PeerServer extends PeerProcess implements Runnable {
    private Thread[] clientConnections;
    public void run() {
        System.out.println("Hello from a server thread!");
        this.clientConnections = new Thread[peerInfo.getSize()];
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            for(int i = 0; i < this.clientConnections.length; i++) {
                Socket clientConnection = serverSocket.accept();
                ServerConnectionHandler newConnection = new ServerConnectionHandler(clientConnection);
                Thread newConnectionThread = new Thread(newConnection);
                this.clientConnections[i] = newConnectionThread;
            }

            for (int i = 0; i < this.clientConnections.length; i++) {
                this.clientConnections[i].start();
            }

            for (int i = 0; i < this.clientConnections.length; i++) {
                this.clientConnections[i].join();
            }

        } catch(Exception e) {
            System.out.print("Whoops! The Server quit unexpectedly!\n" + e + "\n");
        }
    }
}
