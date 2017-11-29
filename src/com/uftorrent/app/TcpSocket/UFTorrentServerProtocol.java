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
                break;
            case 0x3:
                break;
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
                break;
            default:
                break;
        }
        return handleBitField(recievedPayload);
    }

    // Message type 5: bitfield
    private Message handleBitField(byte[] recievedBitfield) {
        System.out.println("Actually handleing a bitfield");
        byte[] emptyBitfield = new byte[bitfield.length];
        byte[] completeBitField = util.getCompleteBitfield(bitfield.length);
        //If the other peer has a completed bitfield, handle it
        if (Arrays.equals(completeBitField, recievedBitfield)) {
            peerInfo.setHasCompleteFile(otherPeerId, true);
        }
        //empty bitfield? Not interested
        if (Arrays.equals(emptyBitfield, recievedBitfield)) {
            // TODO: tw, How do we handle an empty bitfield? Right now, just send an uninterested message
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
    //finds the requestedPiece (a byte value) and returns a message with the piece index as header and piece as payload
    //I am assuming here that the received Payload is only the payload portion of the message (payloadFromInput having been called elsewhere to obtain the payload).
    private Message handleRequest(byte[] receivedPayload) {
        byte pieceIndex = receivedPayload[0];
        // TODO: Get the actual file piece from wherever its being stored
        byte[] returnPayload = new byte[(int)commonVars.getPieceSize()];
        return new Message(1 + pieceIndex, (byte)0x7, returnPayload);
    }
    //handle a have message
    private Message handleHave(byte[] receivedPayload)
    {
        int pieceIndex = (receivedPayload[0] << 24) | (receivedPayload[1]  << 16) | (receivedPayload[2]  << 8) | (receivedPayload[3]);
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
