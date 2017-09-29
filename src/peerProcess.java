import com.uftorrent.app.CommonVars;
import com.uftorrent.app.PeerInfo;
import com.uftorrent.exceptions.InvalidPeerID;

import static java.lang.System.exit;

public class peerProcess {
    private static CommonVars commonVars = new CommonVars();
    private static PeerInfo peerInfo = new PeerInfo();
    private static String peerId;
    private static String hostName;
    private static String portNumber;
    private static String hasCompleteFile;
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
    }
    private static void initPeer(String[] args) {
        try {
            //Initialize this peer Process' info
            peerId = args[0];

            if (peerId.length() != 4) {
                throw new InvalidPeerID("peer ID must have 4 digits.");
            }
            hostName = peerInfo.getHostName(peerId);
            portNumber = peerInfo.getPortNumber(peerId);
            hasCompleteFile = peerInfo.getHasCompleteFile(peerId);

        }
        catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Invalid command line arguments. Please pass in a 4 digit peerProcess ID.");
            exit(1);
        }
        catch (InvalidPeerID ex) {
            System.out.println(ex.getMessage());
            exit(1);
        }
        catch (NullPointerException ex) {
            System.out.println("Invalid peer ID: peer ID provided is not in the peerProcess list.");
            exit(1);
        }
    }
}