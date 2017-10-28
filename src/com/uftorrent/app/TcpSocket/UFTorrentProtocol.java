package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;

public class UFTorrentProtocol extends PeerProcess {
    private String handlingType;
    private String peerID;
    private String otherPeerId;
    private EventLogger eventLogger = new EventLogger();
    public UFTorrentProtocol(String handlingType) {
        this.handlingType = handlingType;
    }
    public String handleInput(String input) {

        if (input.charAt(4) == 'I') {
            return handleHandshake(input);
        }

        if (this.otherPeerId == null) {
            return "Send me a handshake first, then let's talk.";
        }

        String messageLength = input.substring(0, 3);
        int messageType = Character.getNumericValue(input.charAt(4));
        String payload = input.substring(5);

        String response = "";

        switch(messageType) {
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
                handleBitField(payload);
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
    // Handhsake message
    private String handleHandshake(String newHandShake) {
        this.otherPeerId = newHandShake.substring(newHandShake.length() - 4);
        if (this.handlingType.equals("server")) {
            eventLogger.logTCPConnectionFrom(this.otherPeerId);
            return handshakeMessage;
        } else {
            eventLogger.logTCPConnectionTo(this.otherPeerId);
            return "Cya.";
        }
    }
    // Message type 5: bitfield
    private String handleBitField(String newBitField) {
        if (newBitField.equals("1111111111111111")) {
            peerInfo.setHasCompleteFile(otherPeerId, true);
        }
        return "Cya.";
    }
}
