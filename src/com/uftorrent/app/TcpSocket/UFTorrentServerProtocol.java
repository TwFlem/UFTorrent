package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.FilePiece;
import com.uftorrent.app.protocols.Message;
import com.uftorrent.app.utils.Util;
import com.uftorrent.app.setup.env.CommonVars;
import java.util.Arrays;

public class UFTorrentServerProtocol extends PeerProcess {
    private String handlingType;
    private String otherPeerId;
    private EventLogger eventLogger = new EventLogger();
    private Util util = new Util();
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
                break;
            case 0x5:
                return handleBitField(recievedPayload);
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
        byte[] emptyBitfield = new byte[bitfield.length];
        byte[] completeBitField = util.getCompleteBitfield(bitfield.length);
        if (Arrays.equals(completeBitField, recievedBitfield)) {
            peerInfo.setHasCompleteFile(otherPeerId, true);
        }
        if (Arrays.equals(emptyBitfield, recievedBitfield)) {
            // TODO: tw, How do we handle an empty bitfield?
            return new Message(bitfield.length + 1, (byte)0x5, bitfield);
        }
        byte[] interestedBitfield = new byte[4];
        for (int i = 0; i < recievedBitfield.length; i++)
        {
            //bit operations to find what Server has that this client doesn't
            int currentByte = (int)recievedBitfield[i];
            int currentClientByte = (int)bitfield[i];
            currentClientByte = ~currentClientByte;
            int interestedByte = currentClientByte & currentByte;
            interestedBitfield[i] = (byte)interestedByte;
        }
        return new Message(bitfield.length + 1,(byte)0x2, interestedBitfield);
    }
    //finds the requestedPiece (a byte value) and returns a message with the piece index as header and piece as payload
    //I am assuming here that the received Payload is only the payload portion of the message (payloadFromInput having been called elsewhere to obtain the payload).
    private Message handleRequest(byte[] receivedPayload) {
        byte pieceIndex = receivedPayload[0];
        byte[] returnPayload = new byte[(int)commonVars.getPieceSize()];
        return new Message(1 + pieceIndex, (byte)0x7, returnPayload);
    }


    // Return the payload of a message
    private byte[] payloadFromInput(byte[] input) {
        byte[] payload = new byte[input.length - 1 - 5];
        for (int i = 5; i < input.length - 1; i++) {
            payload[i] = input[i];
        }
        return payload;
    }
}
