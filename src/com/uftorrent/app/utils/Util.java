package com.uftorrent.app.utils;

import com.uftorrent.app.protocols.FilePiece;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

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
        int pieceIndex = -1;
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
    public int randomSelection(byte[] interestedPieces)
    {
        int randomSelection = ThreadLocalRandom.current().nextInt(0, interestedPieces.length);
        return randomSelection;
    }
    //determines if a bit is one given an integer index of that bit
    public boolean isBitOne(int index, byte[] bitfield)
    {
        int byteIndex = index/8;
        int offset = index%8;
        int bitChoice = (int)bitfield[byteIndex] >> 7-offset;
        //if the bit is a one, return true
        if ((bitChoice & 1) == 1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    //TODO: test this
    //returns the number of one bits in a byte array. For the logging.
    public int numberOfOnes(byte[] pieces)
    {
        int count = 0;
        for (int i = 0; i < pieces.length * 8; i++)
        {
            int byteIndex = i/8;
            int offset = i%8;
            int bitChoice = (int)pieces[byteIndex] >> 7-offset;
            if ((bitChoice & 1) == 1)
            {
                count = count + 1;
            }
        }
        return count;
    }
    //TODO: test this
    //set a bit to one given an integer index of that bit
    public byte[] setBit(int index, byte[] bitfield)
    {
        int byteIndex = index/8;
        int offset = index%8;
        bitfield[byteIndex] = (byte)(bitfield[byteIndex] | (1 << 7-offset));
        return bitfield;
    }
    //TODO: test this
    public void writeFilePiece(String fileName, FilePiece piece)
    {
        try {
            byte[] bytes = piece.getFilePiece();
            FileOutputStream fos = new FileOutputStream(fileName, true);
            fos.write(bytes);
            fos.close();
        }
        //error catching
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }
    }
}
