package com.uftorrent.app.utils;


import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.exit;

public class Util {
    // Calculate the size from a byte array
    public int messageLengthFromInput(byte[] input) {
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            sum += input[i] * Math.pow(8, i);
        }
        return sum;
    }

    public byte[] subSectionOfByteArray(byte[] b, int start, int end) {
        byte[] newByteArray = new byte[end - start];
        for (int i = start; i < end; i++) {
            newByteArray[i] = b[i];
        }
        return newByteArray;
    }

    // For logging
    public String catStringsFromArrayIntoCSV(String[] arrayOfStrings) {
        String temp = "";
        for (int i =0; i < arrayOfStrings.length; i++) {
            temp = temp + arrayOfStrings[i] + ",";
        }
        return temp;
    }
    public void recursiveDelete(File file) {
        if(file.isDirectory()) {
            if(file.list().length==0) {
                file.delete();
            } else {
                String files[] = file.list();

                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    recursiveDelete(fileDelete);
                }
                if(file.list().length==0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }
    public int packetSize(byte[] arr) {
        ByteBuffer wrapped = ByteBuffer.wrap(arr);
        return wrapped.getInt();
    }
    public byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
    public byte[] getCompleteBitfield(int size) {
        byte[] completeBitField = new byte[size];
        for(int i = 0; i < completeBitField.length; i++) {
            completeBitField[i] = 127;
        }
        return completeBitField;
    }
    //get a piece index from the first four bytes, given the payload (ALREADY STRIPPED OF HEADER INFO)
    public int returnPieceIndex(byte[] receivedPayload)
    {
        int pieceIndex = -1;;
        pieceIndex = (receivedPayload[0] << 24) | (receivedPayload[1]  << 16) | (receivedPayload[2]  << 8) | (receivedPayload[3]);
        return pieceIndex;
    }

    public byte intToBigEndianBitChunk(int i) {
        int sum = 0;
        for (int k = 0; k < i + 1; k++) {
            sum += 1 * Math.pow(2, 8 - k);
        }
        return (byte)sum;
    }
    //function to randomly select a piece from a list of pieces that the Server has that this Client does not
    //maybe I should move this to utils?
    private int randomSelection(int[] interestedPieces)
    {
        int randomSelection = ThreadLocalRandom.current().nextInt(0, interestedPieces.length + 1);
        return randomSelection;
    }
}
