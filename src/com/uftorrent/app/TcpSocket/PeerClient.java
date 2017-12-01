package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;

import java.util.concurrent.TimeUnit;

public class PeerClient extends PeerProcess implements Runnable {
    public void run() {
        try {
            System.out.println("Hello from a client thread!");
            for (int newPeerId : peerInfo.getPeerIds()) {
                if(newPeerId == peerId) {
                    continue;
                }
                ClientConnectionHandler newConnection = new ClientConnectionHandler(
                        peerInfo.getHostName(newPeerId),
                        peerInfo.getPortNumber(newPeerId)
                );
                newConnection.connectionThread = new Thread(newConnection);
                newConnection.connectionThread.run();
            }
        }
        catch(Exception e) {
            System.out.print("Whoops! Client unexpectedly quit!\n" + e + "\n");
        }

        System.out.print("Main Client thread has created all connectionHandlers");
        while(clientConnectionHandlers.keySet().size() < peerInfo.getPeerIds().size() - 1) {
            try {
                TimeUnit.SECONDS.sleep(1);
                System.out.println("Waiting for all handshakes client" + clientConnectionHandlers.keySet().size()
                        + "/" + (peerInfo.getPeerIds().size() - 1));
            } catch (Exception e) {
                System.out.println("Waiting for all client handshakes" + "\n" + e + "\n");
            }
        }
        System.out.print("Main Client thread has shook all hands");

        for (Integer otherPeerId : clientConnectionHandlers.keySet()) {
            try {
                if (otherPeerId == peerId) {
                    clientConnectionHandlers.get(otherPeerId).connectionThread.join();
                    System.out.println("Client for " + otherPeerId + " has closed.");
                }
            } catch (Exception e) {
                System.out.println("Client execution failed\n" + e + "\n");
            }
        }
    }
}
