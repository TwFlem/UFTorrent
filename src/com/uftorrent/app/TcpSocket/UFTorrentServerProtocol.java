package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.FilePiece;
import com.uftorrent.app.protocols.Message;
import com.uftorrent.app.utils.Util;

import java.util.Arrays;

public class UFTorrentServerProtocol extends PeerProcess {
    private int otherPeerId;
    private EventLogger eventLogger = new EventLogger();
    private Util util = new Util();
    public UFTorrentServerProtocol(int otherPeerId) {
        this.otherPeerId = otherPeerId;
    }
    public Message handleInput(byte msgType, byte[] recievedPayload) {
        switch(msgType) {
            case 0x0:
                break;
            case 0x1:
                break;
            case 0x2:
                return handleInterested();
            case 0x3:
                return handleUninterested();
            case 0x4:
                return handleHave(recievedPayload);
            case 0x5:
                return handleBitField(recievedPayload);
            case 0x6:
                return handleRequest(recievedPayload);
            case 0x7:
                return handlePiece(recievedPayload);
            default:
                break;
        }
        return handleBitField(recievedPayload);
    }
    //a server should never be getting choked or unchoked
    //Message type 0: choke
    //Message type 1: unchoke
    //message type 2: interested
    private Message handleInterested() {
        //TODO: Test. probably dont send a message back?
        eventLogger.receiveInterestedMsg(otherPeerId);
        serverConnectionHandlers.get(otherPeerId).isInterestedInMe = true;
        return new Message((byte)0x1);
    }
    //message type 3: uninterested
    private Message handleUninterested() {
        //TODO: Test. probably don't send a message back?
        eventLogger.receiveNotInterestedMsg(otherPeerId);
        serverConnectionHandlers.get(otherPeerId).isInterestedInMe = false;
        return new Message((byte)0x2);
    }
    //message type 4: Have
    //handle a have message, this should be complete
    private Message handleHave(byte[] receivedPayload)
    {
        int pieceIndex = (receivedPayload[0] << 24) | (receivedPayload[1]  << 16) | (receivedPayload[2]  << 8) | (receivedPayload[3]);
        eventLogger.receivedHaveMsg(otherPeerId, pieceIndex);
        //Update other peers bitfield with this info
        serverConnectionHandlers.get(otherPeerId).otherPeersBitfield = util.setBit1(pieceIndex,serverConnectionHandlers.get(otherPeerId).otherPeersBitfield );
        //now find that piece in my bitfield and see if I already have it. If I do, send not interested message. If i dont, send an interested message.
        boolean isOne = util.isBitOne(pieceIndex, bitfield);
        if (isOne)
        {
            //I already have the piece, so I ain't interested
            return new Message((byte)0x3);
        }
        else
        {
            //I don't have the piece, so send an interested message
            return new Message((byte)0x2);
        }
    }
    // Message type 5: bitfield
    private Message handleBitField(byte[] recievedBitfield) {
        //TODO: Test.
        System.out.println("Server Actually handleing a bitfield");
        serverConnectionHandlers.get(this.otherPeerId).otherPeersBitfield = recievedBitfield;
        if (Arrays.equals(fullBitfield, recievedBitfield)) {
            peerInfo.setHasCompleteFile(this.otherPeerId, true);
            serverConnectionHandlers.get(this.otherPeerId).noLongerNeedsToServe = true;
        }
        return new Message(1 + bitfield.length, (byte)0x05, bitfield);
    }
    //message type 6: request
    //should be mostly correct
    private Message handleRequest(byte[] receivedPayload) {
        int pieceIndex = util.returnPieceIndex(receivedPayload);
        pieceIndex = pieceIndex < 0 ? pieceIndex & 0xff : pieceIndex;

        System.out.println("server " + peerId + " handling request for file piece index " + pieceIndex + " for " + this.otherPeerId);
        byte[] newPayload = new byte[receivedPayload.length + pieces[pieceIndex].getFilePiece().length];
        for (int i = 0; i < receivedPayload.length; i++) {
            newPayload[i] = receivedPayload[i];
        }
        for (int i = receivedPayload.length; i < pieces[pieceIndex].getFilePiece().length; i++) {
            newPayload[i] = pieces[pieceIndex].getFilePiece()[i];
        }
        serverConnectionHandlers.get(otherPeerId).otherPeersBitfield = util.setBit1(pieceIndex,serverConnectionHandlers.get(otherPeerId).otherPeersBitfield );
        System.out.println("tw updated other bitfield after receiving " + pieceIndex);
        util.printBitfieldAsBinaryString(serverConnectionHandlers.get(otherPeerId).otherPeersBitfield);
        return new Message(1 + newPayload.length, (byte)0x7, newPayload);
    }
    //message type 7: piece
    private Message handlePiece(byte[] receivedPayload)
    {
        //get a piece with the first 4 bytes as the index. Save it in my piece array, update my bitfield, and continue
        int pieceIndex = util.returnPieceIndex(receivedPayload);
        FilePiece newPiece = new FilePiece(new byte[(int)commonVars.getPieceSize()], pieceIndex);
        //write the bytes into a file piece
        for (int i = 4; i < receivedPayload.length; i++)
        {
            newPiece.getFilePiece()[i-4] = receivedPayload[i];
        }
        //store the file piece
        pieces[pieceIndex] = newPiece;
        //update the bitfield
        bitfield = util.setBit1(pieceIndex, bitfield); //TODO: Test this and make sure it sets properly
        //log it
        int pieceCount = util.numberOfOnes(bitfield);
        eventLogger.downloadedPiece(otherPeerId, pieceIndex, pieceCount);
        //if I have all the pieces, then I should update my status and log it
        if (Arrays.equals(fullBitfield, bitfield))
        {
            eventLogger.downloadComplete(otherPeerId);
        }
        //respond with a request message for a new piece
        // randomally select a new piece to request
        int newRequest = 0;
        byte[] possiblePieces = serverConnectionHandlers.get(otherPeerId).possiblePieces;
        newRequest = util.randomSelection(possiblePieces, pieces.length);
        byte[] bytesOfNewIndex = util.intToByteArray(newRequest);
        return new Message(4 + bytesOfNewIndex.length, (byte)0x6, bytesOfNewIndex);
    }
}
