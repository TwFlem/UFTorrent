package com.uftorrent.app.TcpSocket;


import com.uftorrent.app.main.PeerProcess;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.Vector;
import java.util.Collections;
import java.util.Random;


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
                    newConnection.connectionThread = new Thread(newConnection);
                    newConnection.connectionThread.start();
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

        TimerTask taskOptimisticUnchoking = new TimerTask() {
            @Override
            public void run() {
                Vector<ServerConnectionHandler> chokedNeighbors  = new Vector<ServerConnectionHandler>();
                for (Integer otherPeerId : serverConnectionHandlers.keySet()) {
                    if (serverConnectionHandlers.get(otherPeerId).isChokingClient) {
                        chokedNeighbors.add(serverConnectionHandlers.get(otherPeerId));
                    }
                }
                Random rand = new Random();
                int n = rand.nextInt(chokedNeighbors.size());
                ServerConnectionHandler optimistic = chokedNeighbors.elementAt(n);
                optimistic.unchoke();
                optimistic.connectionThread.resume();
            }
        };
        TimerTask taskChoking = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Choking interval start");
                double downloadRate = 0.0;
                Vector<DownloadRates> preferredNeighbors = new Vector<DownloadRates>();
                Vector<DownloadRates> rates = new Vector<DownloadRates>();

                for (Integer otherPeerId : serverConnectionHandlers.keySet()) {
                    downloadRate = serverConnectionHandlers.get(otherPeerId).totalBytesRead/commonVars.getUnchokingInterval();
                    rates.addElement(new DownloadRates(otherPeerId, downloadRate));
                    serverConnectionHandlers.get(otherPeerId).totalBytesRead = 0;
                }
                Collections.sort(rates);

                String listOfPrefNeighbors = "";
                for (int j = 0; j < commonVars.getNumberOfPrefferedNeighbors(); j++) {
                    preferredNeighbors.addElement(rates.elementAt(j));
                    listOfPrefNeighbors += rates.elementAt(j).peerId + " ";
                }
                System.out.println("List of Preferred Neighbors: " + listOfPrefNeighbors);

                boolean unchokedNeighbor = false;
                for (Integer otherPeerId : serverConnectionHandlers.keySet()) {
                    ServerConnectionHandler otherConnection = serverConnectionHandlers.get(otherPeerId);
                    for (int i = 0; i < preferredNeighbors.size(); i++) {
                        ServerConnectionHandler prefConnection = serverConnectionHandlers.get(preferredNeighbors.elementAt(i).peerId);
                        if (prefConnection == otherConnection) {
                            otherConnection.unchoke();
                            otherConnection.connectionThread.resume();

                            System.out.println("Unchoked PeerId: " + preferredNeighbors.elementAt(i).peerId);
                            unchokedNeighbor = true;
                        }
                    }
                    if (!unchokedNeighbor) {
                        otherConnection.choke();
                        try {
                            otherConnection.connectionThread.suspend();
                        }
                        catch (Exception e) {
                            System.out.println("suspend thread didn't work?");
                        }
                        System.out.println("Unchoked PeerId: " + otherPeerId);
                    }
                }
            }
        };
        timer.schedule(taskChoking, 0, (int)(commonVars.getUnchokingInterval() * 1000));
        timer.schedule(taskOptimisticUnchoking, 0, (int)(commonVars.getOptimisticUnchokingInterval() * 1000));
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
