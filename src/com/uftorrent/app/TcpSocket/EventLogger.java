package com.uftorrent.app.TcpSocket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import com.uftorrent.app.main.PeerProcess;

public class EventLogger extends PeerProcess {
    private void writeToFile(String line) {
        String fileName = "log_peer_" + peerId + ".log";
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw)) {
            out.print(line);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.format("Error: Error appending to %s\n", fileName);
        }
    }
    void logTCPConnectionTo(String otherPeerId) {
        this.writeToFile(String.format("%s: Peer %s makes a connection to Peer %s.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void logTCPConnectionFrom(String otherPeerId) {
        this.writeToFile(String.format("%s: Peer %s is connected from Peer %s.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void changePreferedNeighbor(String[] otherPeerIds) {

        String stringOfPeerIds = util.catStringsFromArrayIntoCSV(otherPeerIds);

        this.writeToFile(String.format("%s: Peer %s is connected from Peer %s.\n",LocalDateTime.now(), peerId, stringOfPeerIds));
    }
    void optimisticallyUnchockedNeighbor(String otherPeerId) {
        this.writeToFile(String.format("%s: Peer %s has the optimistically unchoked neighbor %s.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void unchokedNeighbor(String otherPeerId) {
        this.writeToFile(String.format("%s: Peer %s is unchoked by %s.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void chokeNeighbor(String otherPeerId) {
        this.writeToFile(String.format("%s: Peer %s is choked by %s.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void receivedHaveMsg(String otherPeerId, String pieceIndex) {
        this.writeToFile(String.format("%s: Peer %s received the \'have\' " +
                            "message from %s for the piece %s.\n",LocalDateTime.now(), peerId, otherPeerId, pieceIndex));
    }
    void receiveInteresedMsg(String otherPeerId) {
        this.writeToFile(String.format("%s: Peer %s received the \'interested\' message from %s.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void receiveNotInterestedMsg(String otherPeerId) {
        this.writeToFile(String.format("%s: Peer %s received the \'not interested\' message from %s.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void downloadedPiece(String otherPeerId, String pieceIndex, int numOfPieces) {
        this.writeToFile(String.format("%s: Peer %s has downloaded the piece %s from %s. " +
                            "Now the number of pieces it has is %d.\n",LocalDateTime.now(), peerId, pieceIndex, otherPeerId, numOfPieces));
    }
    void downloadComplete(String peerId) {
        this.writeToFile(String.format("%s: Peer %s has downloaded the complete file.\n",LocalDateTime.now(), peerId));
    }
}
