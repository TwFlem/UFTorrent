package com.uftorrent.app.TcpSocket;


import com.uftorrent.app.main.PeerProcess;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class PeerServer extends PeerProcess implements Runnable {
    public void run() {
        System.out.println("Hello from a server thread!");
        try {
                ServerSocket serverSocket = new ServerSocket(portNumber);
                for (Integer otherPeerId : peerInfo.getPeerIds()) {
                    if (otherPeerId == peerId) {
                        continue;
                    }
                    Socket clientConnection = serverSocket.accept();
                    ServerConnectionHandler newConnection = new ServerConnectionHandler(clientConnection);
                    Thread newConnectionThread = new Thread(newConnection);
                    newConnection.connectionThread = newConnectionThread;
                    newConnectionThread.run();
                }
            } catch(Exception e) {
                System.out.print("Whoops! The Server quit unexpectedly!\n" + e + "\n");
            }

        System.out.print("Main Server thread has created all connectionHandlers");
        while(serverConnectionHandlers.keySet().size() < peerInfo.getPeerIds().size() - 1) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("Waiting for all handshakes server" + serverConnectionHandlers.keySet().size()
                     + "/" + (peerInfo.getPeerIds().size() - 1));
                } catch (Exception e) {
                    System.out.println("Waiting for all handshakes server" + "\n" + e + "\n");
                    }
            }
        System.out.print("Main Sever thread has shook all hands");

        for (Integer otherPeerId : serverConnectionHandlers.keySet()) {
                System.out.println("waiting for servers to finish");
                try {
                    System.out.println("Server for " + otherPeerId + " is waiting to finish.");
                    if (otherPeerId != peerId) {
                        serverConnectionHandlers.get(otherPeerId).connectionThread.join();
                        System.out.println("Server for " + otherPeerId + " has closed.");
                    }
                } catch (Exception e) {
                    System.out.println("Server execution failed\n" + e + "\n");
                }
            }

    }
}
