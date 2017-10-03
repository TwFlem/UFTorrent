package com.uftorrent.app.TcpSocket;

import java.time.LocalDateTime;
import com.uftorrent.app.utils.Util;

public class EventLogger extends TcpSocket {
    private Util util = new Util();
    public void logTCPConnectionTo(String otherPeerId) {
        System.out.format("%s: Peer %s makes a connection to Peer %s.\n",LocalDateTime.now(), peerId, otherPeerId);
    }
    public void logTCPConnectionFrom(String otherPeerId) {
        System.out.format("%s: Peer %s is connected from Peer %s.\n",LocalDateTime.now(), peerId, otherPeerId);
    }
    public void changePreferedNeighbor(String[] otherPeerIds) {

        String stringOfPeerIds = util.catStringsFromArrayIntoCSV(otherPeerIds);

        System.out.format("%s: Peer %s is connected from Peer %s.\n",LocalDateTime.now(), peerId, stringOfPeerIds);
    }
    public void optimisticallyUnchockedNeighbor(String otherPeerId) {
        System.out.format("%s: Peer %s has the optimistically unchoked neighbor %s.\n",LocalDateTime.now(), peerId, otherPeerId);
    }
    public void unchokedNeighbor(String otherPeerId) {
        System.out.format("%s: Peer %s is unchoked by %s.\n",LocalDateTime.now(), peerId, otherPeerId);
    }
    public void chokeNeighbor(String otherPeerId) {
        System.out.format("%s: Peer %s is choked by %s.\n",LocalDateTime.now(), peerId, otherPeerId);
    }
    public void receivedHaveMsg(String otherPeerId, String pieceIndex) {
        System.out.format("%s: Peer %s received the \'have\' " +
                            "message from %s for the piece %s.\n",LocalDateTime.now(), peerId, otherPeerId, pieceIndex);
    }
    public void receiveInteresedMsg(String otherPeerId) {
        System.out.format("%s: Peer %s received the \'interested\' message from %s.\n",LocalDateTime.now(), peerId, otherPeerId);
    }
    public void receiveNotInterestedMsg(String otherPeerId) {
        System.out.format("%s: Peer %s received the \'not interested\' message from %s.\n",LocalDateTime.now(), peerId, otherPeerId);
    }
    public void downloadedPiece(String otherPeerId, String pieceIndex, int numOfPieces) {
        System.out.format("%s: Peer %s has downloaded the piece %s from %s. " +
                            "Now the number of pieces it has is %d.\n",LocalDateTime.now(), peerId, pieceIndex, otherPeerId, numOfPieces);
    }
    public void downloadComplete(String peerId) {
        System.out.format("%s: Peer %s has downloaded the complete file.\n",LocalDateTime.now(), peerId);
    }
}
