package com.uftorrent.app.main;
import com.uftorrent.app.TcpSocket.TcpSocket;
import com.uftorrent.app.setup.env.CommonVars;
import com.uftorrent.app.exceptions.InvalidPeerID;
import com.uftorrent.app.setup.env.PeerInfo;

import static java.lang.System.exit;

public class PeerProcess {
    protected static final CommonVars commonVars = new CommonVars();
    protected static final PeerInfo peerInfo = new PeerInfo();
    protected static String peerId;
    protected static String hostName;
    protected static String portNumber;
    protected static String hasCompleteFile;
    protected static String handshakeMessage = "P2PFILESHARINGPROJ0000000000";
    public static void main(String[] args) {

        initPeer(args);

        //Will make jUnit tests one day
        System.out.println("Here's our env variables!");
        commonVars.print();

        System.out.println("Here's all Peer Info!");
        peerInfo.print();

        //Testing get methods
        System.out.format("Number of preferred neighbors: %s%n", commonVars.get("NumberOfPreferredNeighbors"));
        System.out.format("ID's: %s hostName: %s%n", "1004", peerInfo.getHostName("1004"));

        //Display this peer's info
        System.out.println("Here's this Peer's Info!");
        System.out.format("ID: %s HostName: %s, PortNumber: %s, HasCompleteFile: %s%n",
                peerId, hostName, portNumber, hasCompleteFile);

        TcpSocket connection = new TcpSocket();

        System.out.println("check out the sick logging class too!");
        connection.simulateLogs();
    }

    private static void initPeer(String[] args) {
        try {
            //Initialize this peer Process' info.
            peerId = args[0];

            if (peerId.length() != 4) {
                throw new InvalidPeerID("peer ID must have 4 digits.");
            }
            hostName = peerInfo.getHostName(peerId);
            portNumber = peerInfo.getPortNumber(peerId);
            hasCompleteFile = peerInfo.getHasCompleteFile(peerId);

        }
        catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Invalid command line arguments. Please pass in a 4 digit com.uftorrent.app.main.PeerProcess ID.");
            exit(1);
        }
        catch (InvalidPeerID ex) {
            System.out.println(ex.getMessage());
            exit(1);
        }
        catch (NullPointerException ex) {
            System.out.println("Invalid peer ID: peer ID provided is not in the com.uftorrent.app.main.PeerProcess list.");
            exit(1);
        }
    }
}