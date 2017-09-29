import com.uftorrent.app.CommonVars;
import com.uftorrent.app.PeerInfo;

public class UFTorrent {
    public static CommonVars commonVars = new CommonVars();
    public static PeerInfo peerInfo = new PeerInfo();
    public static void main(String[] args) {
        System.out.println("Here's our env variables!");
        commonVars.print();
        System.out.println("Here's our Peer Info!");
        peerInfo.print();
        System.out.format("Number of preferred neighbors: %s%n", commonVars.get("NumberOfPreferredNeighbors"));
        System.out.format("ID's: %s HostName: %s%n", "1004", peerInfo.getHostName("1004"));
    }
}