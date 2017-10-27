package com.uftorrent.app.protocols;

import java.util.Arrays;

public class Message {
    public final int DATA_CHOKE = 0;
    public final int DATA_UNCHOKE = 1;
    public final int DATA_INTERESTED = 2;
    public final int DATA_UNINTERESTED = 3;
    public final int DATA_HAVE = 4;
    public final int DATA_BITFIELD = 5;
    public final int DATA_REQUEST = 6;
    public final int DATA_PIECE = 7;
    //message length may need to be made into a byte array for consistency,
    //but a int IS 4 bytes long
    private int messageLength;
    private int pieceIndex;
    private byte[] data;
    private byte messageType;
    //------------------Constructors  down here ----------------------------------------------------
    public Message(int messageLength, int pieceIndex, byte[] data, byte messageType) {
        this.messageLength = messageLength;
        this.pieceIndex = pieceIndex;
        this.data = data;
        this.messageType = messageType;
    }
    public Message(byte messageType) {
        if (messageType == DATA_CHOKE || messageType == DATA_UNCHOKE ||
                messageType == DATA_INTERESTED || messageType == DATA_UNINTERESTED)
        {
            this.messageLength = 0;
            this.data = new byte[0];
            this.pieceIndex = 0;
        }
        else
        {
            System.out.println("Not a valid constructor");
        }
        this.messageType = messageType;
    }


    //-------------------------Other methods down here-------------------------------------------------
    public int getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    public int getLength() {
        return messageLength;
    }

    public void setLength(int length) {
        this.messageLength = messageLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }
    //Return a piece index if the message is of the proper type to have one
    public int returnPieceIndex()
    {
        int type = this.getMessageType();
        if (type == DATA_HAVE || type == DATA_REQUEST || type == DATA_PIECE)
        {
            int pieceIndex = this.getData()[0];
        }
        else
        {
            System.out.println("This isn't a valid message type to have a Piece Index");
        }
        return pieceIndex;
    }
}

