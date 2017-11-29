package com.uftorrent.app.TcpSocket;


import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class PeerServer extends PeerProcess implements Runnable {
    public void run() {
        System.out.println("Hello from a server thread!");
        try {
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Set<Integer> otherPeerIds = peerInfo.getPeerIds();
                for (int i = 0; i < otherPeerIds.size(); i++) {
                    Socket clientConnection = serverSocket.accept();
                    ServerConnectionHandler newConnection = new ServerConnectionHandler(clientConnection);
                    Thread newConnectionThread = new Thread(newConnection);
                    newConnection.connectionThread = newConnectionThread;
                    newConnectionThread.run();
                }
            } catch(Exception e) {
                System.out.print("Whoops! The Server quit unexpectedly!\n" + e + "\n");
            }

            while(serverConnectionHandlers.keySet().size() == 0) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                        System.out.println("Waiting for at least one handshake" + "\n" + e + "\n");
                    }
            }

            for (Integer otherPeerId : serverConnectionHandlers.keySet()) {
                    System.out.println("waiting for servers to finish");
                try {
                    serverConnectionHandlers.get(otherPeerId).connectionThread.join();
                } catch (Exception e) {
                    System.out.println("Server execution failed\n" + e + "\n");
                }
            }

    }
}
