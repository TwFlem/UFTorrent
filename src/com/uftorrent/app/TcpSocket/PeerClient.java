package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;

public class PeerClient extends PeerProcess implements Runnable {

    private EventLogger eventLogger = new EventLogger();
    private Thread[] serverConnections;
    public void run() {
        try {
            System.out.println("Hello from a client thread!");
            this.serverConnections = new Thread[peerInfo.getSize()];

            int peerIndex = 0;
            for (int peerID : peerInfo.getPeerIds()) {
                System.out.println("hello " + peerID);
                ClientConnectionHandler newConnection = new ClientConnectionHandler(
                        peerInfo.getHostName(peerID),
                        peerInfo.getPortNumber(peerID)
                );
                Thread newConnectionThread = new Thread(newConnection);
                this.serverConnections[peerIndex] = newConnectionThread;
                peerIndex = peerIndex + 1;
            }

            for (int i = 0; i < this.serverConnections.length; i++) {
                this.serverConnections[i].start();
            }

            for (int i = 0; i < this.serverConnections.length; i++) {
                this.serverConnections[i].join();
            }

        }
        catch(Exception e) {
            System.out.print("Whoops! Client unexpectedly quit!\n" + e + "\n");
        }
    }
}
