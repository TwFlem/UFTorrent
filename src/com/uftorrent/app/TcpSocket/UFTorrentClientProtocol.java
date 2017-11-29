package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;
import com.uftorrent.app.utils.Util;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class UFTorrentClientProtocol extends PeerProcess {
    private EventLogger eventLogger = new EventLogger();
    private int otherPeerId;
    private Util util = new Util();
    public UFTorrentClientProtocol(int otherPeerId) {
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
                strippedPayload = payloadFromInput(recievedPayload);
                return handleHave(strippedPayload);
            case 0x5:
                return handleBitField(recievedPayload);
            case 0x6:
                break;
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
        if (Arrays.equals(emptyBitfield, recievedBitfield)) {
            // TODO: tw, How do we handle an empty bitfield?
            return new Message(bitfield.length + 1, (byte)0x5, bitfield);
        }
        return new Message(bitfield.length + 1, (byte)0x5, bitfield);
    }
    //Mesage
    private Message handleHave(byte[] receivedPayload)
    {
        byte[] interestedBitfield = new byte[4];
        for (int i = 0; i < receivedPayload.length; i++)
        {
            //bit operations to find what Server has that this client doesn't
            int currentByte = (int)receivedPayload[i];
            int currentClientByte = (int)bitfield[i];
            currentClientByte = ~currentClientByte;
            int interestedByte = currentClientByte & currentByte;
            interestedBitfield[i] = (byte)interestedByte;
        }
        return new Message(4+1,(byte)0x2, interestedBitfield);
    }
    private byte[] payloadFromInput(byte[] input) {
        byte[] payload = new byte[input.length - 1 - 5];
        for (int i = 5; i < input.length - 1; i++) {
            payload[i] = input[i];
        }
        return payload;
    }
}


