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
        byte[] strippedPayload;
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
        clientConnectionHandlers.get(peerId).isChoked = true;
        return new Message((byte)0x0);
    }
    //Message type 1: unchoke
    private Message handleUnchoke() {
        //TODO: Test
        eventLogger.unchokedNeighbor(Integer.toString(otherPeerId));
        clientConnectionHandlers.get(peerId).isChoked = false;
        byte[] possiblePieces = clientConnectionHandlers.get(peerId).possiblePieces;
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
        //TODO: update the list of interested peers, probably don't send a message back?
        eventLogger.receiveNotInterestedMsg(Integer.toString(otherPeerId));
        return new Message((byte)0x2);
    }
    //message type 4: Have
    //handle a have message, this should be complete
    private Message handleHave(byte[] receivedPayload)
    {
        int pieceIndex = (receivedPayload[0] << 24) | (receivedPayload[1]  << 16) | (receivedPayload[2]  << 8) | (receivedPayload[3]);
        eventLogger.receivedHaveMsg(Integer.toString(otherPeerId), Integer.toString(pieceIndex));
        //now find that piece in my bitfield and see if I already have it. If I do, send not interested message. If i dont, send an interested message.
        int byteIndex = pieceIndex/8;
        int offset = pieceIndex%8;
        int bytef = (int)bitfield[byteIndex];
        int bitChoice = (int)bitfield[byteIndex] >> 7-offset;
        if ((bitChoice & 1) == 1)
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
        //TODO: Store a list of peers with data I'm interested in somewhere
        //TODO: Actually store a list of the interesting pieces somewhere
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
        // TODO: Just make sure this is how were storing pieces, apart from that, this one should be good to go
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
        int byteIndex = pieceIndex/8;
        int offset = pieceIndex%8;
        bitfield[byteIndex] = (byte)(bitfield[byteIndex] | (1 << 7-offset)); //TODO: Test this and make sure it sets properly
        eventLogger.downloadedPiece(Integer.toString(otherPeerId),Integer.toString(pieceIndex), 5); //TODO: Write util to determine how many pieces I currently have
        //if I have all the pieces, then I should update my status and log it
        if (Arrays.equals(completeBitField, bitfield))
        {
            eventLogger.downloadComplete(Integer.toString(otherPeerId));
        }
        //respond with a request message for a new piece
        // TODO: Figure out how to determine next piece to request (need list of interested pieces to be stored somewhere
        int newRequest = 0;
        byte[] bytesOfNewIndex = util.intToByteArray(newRequest);
        return new Message(4 + bytesOfNewIndex.length, (byte)0x6, bytesOfNewIndex);
    }
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


