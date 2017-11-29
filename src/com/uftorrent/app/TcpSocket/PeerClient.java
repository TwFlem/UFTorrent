package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;

import java.util.concurrent.TimeUnit;

public class PeerClient extends PeerProcess implements Runnable {

    private EventLogger eventLogger = new EventLogger();
    public void run() {
        try {
            System.out.println("Hello from a client thread!");
            for (int newPeerId : peerInfo.getPeerIds()) {
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

        while(clientConnectionHandlers.keySet().size() == 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                    System.out.println("Waiting for at least one handshake" + "\n" + e + "\n");
                }
        }

        for (Integer otherPeerId : clientConnectionHandlers.keySet()) {
            try {
                clientConnectionHandlers.get(otherPeerId).connectionThread.join();
            } catch (Exception e) {
                System.out.println("Client execution failed\n" + e + "\n");
            }
        }
    }
}
