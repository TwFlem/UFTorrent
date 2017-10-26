package com.uftorrent.app.TcpSocket;
import com.uftorrent.app.main.PeerProcess;

public class Client extends PeerProcess {
    public Client() {
    }
    public void simulateLogs() {
        EventLogger logger = new EventLogger();
        String[] prefferedNeighbors = {"1000", "1001", "1002"};
        logger.logTCPConnectionTo("9999");
        logger.logTCPConnectionFrom("9999");
        logger.changePreferedNeighbor(prefferedNeighbors);
        logger.optimisticallyUnchockedNeighbor("9999");
        logger.unchokedNeighbor("9999");
        logger.chokeNeighbor("9999");
        logger.receivedHaveMsg("9999", "some piece index");
        logger.receiveInteresedMsg("9999");
        logger.receiveNotInterestedMsg("9999");
        logger.downloadedPiece("9999", "some index", 420);
        logger.downloadComplete("5000");
    }
}
