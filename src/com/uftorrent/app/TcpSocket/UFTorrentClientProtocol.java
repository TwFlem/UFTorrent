package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;
import com.uftorrent.app.utils.Util;
import com.uftorrent.app.protocols.FilePiece;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import com.uftorrent.app.TcpSocket.ClientConnectionHandler;
public class UFTorrentClientProtocol extends PeerProcess {
    private EventLogger eventLogger = new EventLogger();
    private int otherPeerId;
    private Util util = new Util();
    public UFTorrentClientProtocol(int otherPeerId) {
        this.otherPeerId = otherPeerId;
        clientConnectionHandlers.get(this.otherPeerId);
    }
    public Message handleInput(byte msgType, byte[] recievedPayload) {
        switch(msgType) {
            case 0x0:
                return handleChoke();
            case 0x1:
                return handleUnchoke();
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

    //Message type 0: choke
    private Message handleChoke() {
        //TODO: Is this fine for choking? This probably shouldn't actually return a message at all, maybe implement a -1 return message?.
        eventLogger.chokeNeighbor(Integer.toString(otherPeerId));
        clientConnectionHandlers.get(otherPeerId).isChoked = true;
        return new Message((byte)0x0);
    }
    //Message type 1: unchoke
    private Message handleUnchoke() {
        //TODO: Test
        eventLogger.unchokedNeighbor(Integer.toString(otherPeerId));
        clientConnectionHandlers.get(otherPeerId).isChoked = false;
        byte[] possiblePieces = clientConnectionHandlers.get(otherPeerId).possiblePieces;
        int requestedPiece = util.randomSelection(possiblePieces);
        byte[] requestedArray = util.intToByteArray(requestedPiece);
        return new Message(5,(byte)0x6, requestedArray);
    }
    //message type 2: interested
    private Message handleInterested() {
        //TODO: Test. probably dont send a message back?
        eventLogger.receiveInteresedMsg(Integer.toString(otherPeerId));
        clientConnectionHandlers.get(otherPeerId).isInterested = true;
        return new Message((byte)0x2);
    }
    //message type 3: uninterested
    private Message handleUninterested() {
        //TODO: Test. probably don't send a message back?
        eventLogger.receiveNotInterestedMsg(Integer.toString(otherPeerId));
        clientConnectionHandlers.get(otherPeerId).isInterested = true;
        return new Message((byte)0x2);
    }
    //message type 4: Have
    //handle a have message, this should be complete
    //TODO: Test this.
    private Message handleHave(byte[] receivedPayload)
    {
        int pieceIndex = (receivedPayload[0] << 24) | (receivedPayload[1]  << 16) | (receivedPayload[2]  << 8) | (receivedPayload[3]);
        eventLogger.receivedHaveMsg(Integer.toString(otherPeerId), Integer.toString(pieceIndex));
        //Update other peers bitfield with this info
        clientConnectionHandlers.get(otherPeerId).otherPeersBitfield = util.setBit(pieceIndex,clientConnectionHandlers.get(otherPeerId).otherPeersBitfield );
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
        System.out.println("Client Actually handleing a bitfield");
        byte[] emptyBitfield = new byte[bitfield.length];
        byte[] completeBitField = util.getCompleteBitfield(bitfield.length);
        //If the other peer has a completed bitfield, handle it
        if (Arrays.equals(completeBitField, recievedBitfield)) {
            peerInfo.setHasCompleteFile(otherPeerId, true);
        }
        //empty bitfield? Not interested
        if (Arrays.equals(emptyBitfield, recievedBitfield)) {
            return new Message((byte)0x3);
        }
        byte[] interestedBitfield = new byte[bitfield.length];
        for (int i = 0; i < recievedBitfield.length; i++)
        {
            //bit operations to find what Server has that this client doesn't
            int currentByte = (int)recievedBitfield[i];
            int currentClientByte = (int)bitfield[i];
            currentClientByte = ~currentClientByte;
            int interestedByte = currentClientByte & currentByte;
            interestedBitfield[i] = (byte)interestedByte;
        }
        //store the interested bitfield for later reference
        clientConnectionHandlers.get(otherPeerId).otherPeersBitfield = recievedBitfield;
        clientConnectionHandlers.get(otherPeerId).possiblePieces = interestedBitfield;
        //no files interested in, send a not interested message
        if (Arrays.equals(emptyBitfield, interestedBitfield))
        {
            return new Message((byte)0x3);
        }
        //otherwise, send an interested message to let the other peer know I'm interested in its pieces.
        return new Message((byte)0x2);
    }
    //message type 6: request
    //should be mostly correct
    //TODO: Test this.
    private Message handleRequest(byte[] receivedPayload) {
        int pieceIndex = util.returnPieceIndex(receivedPayload);
        FilePiece returnPiece = pieces[pieceIndex];
        byte[] returnPayload = returnPiece.getFilePiece();
        return new Message(1 + pieceIndex, (byte)0x7, returnPayload);
    }
    //message type 7: piece
    //TODO: Test this.
    //TODO: Blast out have messages after downloading a piece
    //TODO: Check against all bitfields after receiving piece, to determine if should now send not interested message
    private Message handlePiece(byte[] receivedPayload)
    {
        //get a piece with the first 4 bytes as the index. Save it in my piece array, update my bitfield, and continue
        int pieceIndex = util.returnPieceIndex(receivedPayload);
        byte[] completeBitField = util.getCompleteBitfield(bitfield.length);
        byte[] emptyBitfield = new byte[bitfield.length];
        FilePiece newPiece = new FilePiece(new byte[(int)commonVars.getPieceSize()], pieceIndex);
        //write the bytes into a file piece
        for (int i = 4; i < receivedPayload.length; i++)
        {
            newPiece.getFilePiece()[i-4] = receivedPayload[i];
        }
        //store the file piece
        pieces[pieceIndex] = newPiece;
        //update the bitfield
        bitfield = util.setBit(pieceIndex, bitfield); //TODO: Test this and make sure it sets properly
        //log it
        int pieceCount = util.numberOfOnes(bitfield);
        eventLogger.downloadedPiece(Integer.toString(otherPeerId),Integer.toString(pieceIndex), pieceCount);
        //if I have all the pieces, then I should update my status and log it
        if (Arrays.equals(completeBitField, bitfield))
        {
            eventLogger.downloadComplete(Integer.toString(otherPeerId));
            for (int i = 0; i < bitfield.length; i++)
            {
                util.writeFilePiece(commonVars.getFileName(), pieces[i]); //TODO: test this and make sure the filename is right
            }
        }
        //respond with a request message for a new piece
        // randomally select a new piece to request
        int newRequest = 0;
        byte[] possiblePieces = clientConnectionHandlers.get(otherPeerId).possiblePieces;
        //check to see if there are no possible pieces, if so, send a not interested message
        if (Arrays.equals(emptyBitfield, possiblePieces)) {
            return new Message((byte)0x3);
        }
        newRequest = util.randomSelection(possiblePieces);
        byte[] bytesOfNewIndex = util.intToByteArray(newRequest);
        return new Message(4 + bytesOfNewIndex.length, (byte)0x6, bytesOfNewIndex);
    }
}


