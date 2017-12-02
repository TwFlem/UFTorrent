package com.uftorrent.app.TcpSocket;


import com.uftorrent.app.main.PeerProcess;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.Vector;


public class PeerServer extends PeerProcess implements Runnable {

    private EventLogger eventLogger = new EventLogger();
    Timer timer = new Timer();
    public void run() {
        System.out.println("Hello from a server thread!");
        try {
                ServerSocket serverSocket = new ServerSocket(portNumber);
                for (Integer otherPeerId : peerInfo.getPeerIds()) {
                    if (otherPeerId == peerId) {
                        continue;
                    }
                    Socket clientConnection = serverSocket.accept();
                    eventLogger.logTCPConnectionTo(otherPeerId);
                    ServerConnectionHandler newConnection = new ServerConnectionHandler(clientConnection);
                    Thread newConnectionThread = new Thread(newConnection);
                    newConnection.connectionThread = newConnectionThread;
                    newConnectionThread.start();
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

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                double downRatePref1 = 0.0;
                double downRatePref2 = 0.0;
                Vector<ServerConnectionHandler> preferredNeighbors = new Vector<ServerConnectionHandler>();
                ServerConnectionHandler prefer1 = null;
                ServerConnectionHandler prefer2 = null;
                if (serverConnectionHandlers.size() > 1) {
                    for (Integer otherPeerId : serverConnectionHandlers.keySet()) {
                        if (serverConnectionHandlers.get(otherPeerId).downloadRate >= downRatePref1) {
                            prefer1 = serverConnectionHandlers.get(otherPeerId);
                        } else if (serverConnectionHandlers.get(otherPeerId).downloadRate >= downRatePref2 && serverConnectionHandlers.get(otherPeerId).downloadRate < downRatePref1) {
                            prefer2 = serverConnectionHandlers.get(otherPeerId);
                        }

                    }
                    preferredNeighbors.addElement(prefer2);
                }
                preferredNeighbors.addElement(prefer1);
                boolean unchokedNeighbor = false;
                for (Integer otherPeerId : serverConnectionHandlers.keySet()) {
                    for (int i = 0; i < preferredNeighbors.size(); i++) {
                        if (serverConnectionHandlers.get(otherPeerId) == preferredNeighbors.elementAt(i) && serverConnectionHandlers.get(otherPeerId).isChokingTheOtherPeer == true) {
                            serverConnectionHandlers.get(otherPeerId).unchoke();
                            unchokedNeighbor = true;
                        }
                    }
                    if (!unchokedNeighbor) {
                        serverConnectionHandlers.get(otherPeerId).choke();
                    }
                }
            }
        };
        timer.schedule(task, 0, (int)(commonVars.getUnchokingInterval() * 1000));
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
