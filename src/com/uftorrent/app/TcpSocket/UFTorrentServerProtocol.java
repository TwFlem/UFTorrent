package com.uftorrent.app.TcpSocket;

import com.uftorrent.app.main.PeerProcess;
import com.uftorrent.app.protocols.FilePiece;
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
        byte[] emptyBitfield = new byte[bitfield.length];
        byte[] completeBitField = util.getCompleteBitfield(bitfield.length);
        byte[] hasPieces = new byte[bitfield.length];
        if (Arrays.equals(completeBitField, recievedBitfield)) {
            peerInfo.setHasCompleteFile(otherPeerId, true);
        }
        /*Run through each byte. For each byte, flip the bits on the clients sent bitfield.
        Now, a clientside 1 means it doesn't have that file. A serverside one means it does have the file.
        bitwise AND these together, and the result is a bitfield where a 1 represents a file that the server has
        and the client does not. */
        else

        {
            for (int i = 0; i < bitfield.length; i++)
            {
                hasPieces[i] = (byte)(~recievedBitfield[i] & bitfield[i]);
            }
        }
        if (Arrays.equals(emptyBitfield, recievedBitfield)) {
            // TODO: tw, How do we handle an empty bitfield? S
            return new Message(3, (byte)0x5, bitfield);
        }
        return new Message(1 + hasPieces.length, (byte)0x5, hasPieces);
    }

    // Return the payload of a message
    private byte[] payloadFromInput(byte[] input) {
        byte[] payload = new byte[input.length - 1 - 5];
        for (int i = 5; i < input.length - 1; i++) {
            payload[i] = input[i];
        }
        return payload;
    }
    //Message type 7: receive a request message, send back a piece message with the requested piece

}
