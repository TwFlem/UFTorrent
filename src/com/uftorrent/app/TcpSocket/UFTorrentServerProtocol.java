package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.FilePiece;
import com.uftorrent.app.protocols.Message;
import com.uftorrent.app.utils.Util;
import com.uftorrent.app.setup.env.CommonVars;
import java.util.Arrays;

public class UFTorrentServerProtocol extends PeerProcess {
    private String handlingType;
    private int otherPeerId;
    private EventLogger eventLogger = new EventLogger();
    private Util util = new Util();
    public UFTorrentServerProtocol(int otherPeerId) {
        this.otherPeerId = otherPeerId;
    }
    public Message handleInput(byte msgType, byte[] recievedPayload) {
        byte[] strippedPayload;
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
                System.out.println("Handleing a have");
                strippedPayload = payloadFromInput(recievedPayload);
                return handleHave(strippedPayload);
            case 0x5:
                System.out.println("Handleing a bitfield");
                strippedPayload = payloadFromInput(recievedPayload);
                return handleBitField(strippedPayload);
            case 0x6:
                strippedPayload = payloadFromInput(recievedPayload);
                return handleRequest(strippedPayload);
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
        eventLogger.receiveInteresedMsg(Integer.toString(otherPeerId));
        serverConnectionHandlers.get(otherPeerId).isNotInteresting = false;
        return new Message((byte)0x2);
    }
    //message type 3: uninterested
    private Message handleUninterested() {
        //TODO: Test. probably don't send a message back?
        eventLogger.receiveNotInterestedMsg(Integer.toString(otherPeerId));
        serverConnectionHandlers.get(otherPeerId).isNotInteresting = true;
        return new Message((byte)0x2);
    }
    //message type 4: Have
    //handle a have message, this should be complete
    private Message handleHave(byte[] receivedPayload)
    {
        int pieceIndex = (receivedPayload[0] << 24) | (receivedPayload[1]  << 16) | (receivedPayload[2]  << 8) | (receivedPayload[3]);
        eventLogger.receivedHaveMsg(Integer.toString(otherPeerId), Integer.toString(pieceIndex));
        //Update other peers bitfield with this info
        serverConnectionHandlers.get(otherPeerId).otherPeersBitfield = util.setBit(pieceIndex,serverConnectionHandlers.get(otherPeerId).otherPeersBitfield );
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
        System.out.println("Actually handleing a bitfield");
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
        serverConnectionHandlers.get(otherPeerId).otherPeersBitfield = recievedBitfield;
        serverConnectionHandlers.get(otherPeerId).possiblePieces = interestedBitfield;
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
    private Message handleRequest(byte[] receivedPayload) {
        int pieceIndex = util.returnPieceIndex(receivedPayload);
        FilePiece returnPiece = pieces[pieceIndex];
        byte[] returnPayload = returnPiece.getFilePiece();
        return new Message(1 + pieceIndex, (byte)0x7, returnPayload);
    }
    //message type 7: piece
    private Message handlePiece(byte[] receivedPayload)
    {
        //get a piece with the first 4 bytes as the index. Save it in my piece array, update my bitfield, and continue
        int pieceIndex = util.returnPieceIndex(receivedPayload);
        byte[] completeBitField = util.getCompleteBitfield(bitfield.length);
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
        }
        //respond with a request message for a new piece
        // randomally select a new piece to request
        int newRequest = 0;
        byte[] possiblePieces = serverConnectionHandlers.get(otherPeerId).possiblePieces;
        newRequest = util.randomSelection(possiblePieces);
        byte[] bytesOfNewIndex = util.intToByteArray(newRequest);
        return new Message(4 + bytesOfNewIndex.length, (byte)0x6, bytesOfNewIndex);
    }
    // Return the payload of a message
    private byte[] payloadFromInput(byte[] input) {
        byte[] payload = new byte[input.length - 5];
        for (int i = 5; i < input.length; i++) {
            payload[i-5] = input[i];
            System.out.print(payload[i-5] + " ");
        }
        System.out.println("Returned Payload length" + payload.length);
        return payload;
    }
}
