package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;

public class UFTorrentProtocol extends PeerProcess {
    private String handlingType;
    private String otherPeerId;
    private EventLogger eventLogger = new EventLogger();
    public UFTorrentProtocol(String handlingType, String otherPeerId) {
        this.handlingType = handlingType;
        this.otherPeerId = otherPeerId;
    }
    public String handleInput(String input) {

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

    // Message type 5: bitfield
    private String handleBitField(String newBitField) {
        if (newBitField.equals("1111111111111111")) {
            peerInfo.setHasCompleteFile(otherPeerId, true);
        }
        return "Cya.";
    }
}
