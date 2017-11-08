package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.Message;
import com.uftorrent.app.utils.Util;

import java.util.Arrays;

public class UFTorrentServerProtocol extends PeerProcess {
    private String handlingType;
    private String otherPeerId;
    private EventLogger eventLogger = new EventLogger();
    private Util util = new Util();
    public UFTorrentServerProtocol(String handlingType, String otherPeerId) {
        this.handlingType = handlingType;
        this.otherPeerId = otherPeerId;
    }
    public Message handleInput(byte msgType, byte[] recievedPayload) {

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
        byte[] completedBitField = {7, 7};
        byte[] emptyBitfield = {0, 0};
        if (Arrays.equals(completedBitField, recievedBitfield)) {
            peerInfo.setHasCompleteFile(otherPeerId, true);
        }
        if (Arrays.equals(completedBitField, recievedBitfield)) {
            // TODO: tw, How do we handle an empty bitfield?
            return new Message(3, (byte)0x5, bitfield);
        }
        return new Message(3, (byte)0x5, bitfield);
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