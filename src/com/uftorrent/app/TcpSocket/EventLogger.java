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
    void logTCPConnectionTo(int otherPeerId) {
        this.writeToFile(String.format("%s: Peer %d makes a connection to Peer %d.\n", LocalDateTime.now(), peerId, otherPeerId));
    }
    void logTCPConnectionFrom(int otherPeerId) {
        this.writeToFile(String.format("%s: Peer %d is connected from Peer %d.\n", LocalDateTime.now(), peerId, otherPeerId));
    }
    void changePreferedNeighbor(int[] otherPeerIds) {

        String stringOfPeerIds = util.catStringsFromArrayIntoCSV(otherPeerIds);

        this.writeToFile(String.format("%s: Peer %d is connected from Peer %s.\n", LocalDateTime.now(), peerId, stringOfPeerIds));
    }
    void optimisticallyUnchockedNeighbor(int otherPeerId) {
        this.writeToFile(String.format("%s: Peer %d has the optimistically unchoked neighbor %d.\n", LocalDateTime.now(), peerId, otherPeerId));
    }
    void unchokedNeighbor(int otherPeerId) {
        this.writeToFile(String.format("%s: Peer %d is unchoked by %d.\n", LocalDateTime.now(), peerId, otherPeerId));
    }
    void chokeNeighbor(int otherPeerId) {
        this.writeToFile(String.format("%s: Peer %d is choked by %d.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void receivedHaveMsg(int otherPeerId, int pieceIndex) {
        this.writeToFile(String.format("%s: Peer %d received the \'have\' " +
                            "message from %s for the piece %d.\n", LocalDateTime.now(), peerId, otherPeerId, pieceIndex));
    }
    void receiveInteresedMsg(int otherPeerId) {
        this.writeToFile(String.format("%s: Peer %d received the \'interested\' message from %d.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void receiveNotInterestedMsg(int otherPeerId) {
        this.writeToFile(String.format("%s: Peer %d received the \'not interested\' message from %d.\n",LocalDateTime.now(), peerId, otherPeerId));
    }
    void downloadedPiece(int otherPeerId, int pieceIndex, int numOfPieces) {
        this.writeToFile(String.format("%s: Peer %d has downloaded the piece %s from %d. " +
                            "Now the number of pieces it has is %d.\n", LocalDateTime.now(), peerId, pieceIndex, otherPeerId, numOfPieces));
    }
    void downloadComplete(int peerId) {
        this.writeToFile(String.format("%s: Peer %d has downloaded the complete file.\n",LocalDateTime.now(), peerId));
    }
}
