package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.utils.Util;

import java.util.Arrays;

public class UFTorrentClientProtocol extends PeerProcess {
    private String handlingType;
    private String otherPeerId;
    private EventLogger eventLogger = new EventLogger();
    private Util util = new Util();

    public UFTorrentClientProtocol(String handlingType, String otherPeerId) {
        this.handlingType = handlingType;
        this.otherPeerId = otherPeerId;
    }

    public String handleInput(byte[] input) {

        int messageType = input[4];

        String response = "";

        switch (messageType) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            default:
                response = handshakeMessage;
                break;
        }
        return response;
    }

    // Message type 5: bitfield
    private String handleBitField(byte[] newBitField) {
        byte[] completedBitField = {7, 7};
        if (Arrays.equals(completedBitField, newBitField)) {
            peerInfo.setHasCompleteFile(otherPeerId, true);
            return "Cya.";
        }
        return "Cya.";
    }

    //message type 7: request
    private String handleRequest(byte[] requestMessage) {
        return "something";

    }
}