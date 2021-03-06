package com.uftorrent.app.utils;

import com.uftorrent.app.protocols.FilePiece;
import com.uftorrent.app.protocols.Message;
import com.uftorrent.app.setup.env.CommonVars;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
    public String catStringsFromArrayIntoCSV(int[] arrayOfStrings) {
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
    public int byteArrayToInt(byte[] arr) {
        ByteBuffer wrapped = ByteBuffer.wrap(arr);
        return wrapped.getInt();
    }
    public byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
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
    public int randomSelection(byte[] interestedPieces, int filePieceSize)
    {
        ArrayList<Integer> poolOfRandomFilePieceIndecies = new ArrayList<>();

        for (int i = 0; i < filePieceSize; i++) {
            if (this.isBitOne(i, interestedPieces)) {
                poolOfRandomFilePieceIndecies.add(i);
            }
        }
        int randomSelection = ThreadLocalRandom.current().nextInt(0, poolOfRandomFilePieceIndecies.size());
        System.out.println("tw total interested pieces " + poolOfRandomFilePieceIndecies.size());
        return poolOfRandomFilePieceIndecies.get(randomSelection);
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
    public byte[] setBit1(int index, byte[] bitfield)
    {
        int byteIndex = index/8;
        int offset = index%8;
        bitfield[byteIndex] = (byte)(bitfield[byteIndex] | (1 << 7-offset));
        return bitfield;
    }

    public byte[] setBit0(int index, byte[] bitfield)
    {
        int byteIndex = index/8;
        int offset = index%8;
        bitfield[byteIndex] = (byte)(bitfield[byteIndex] & ~(1 << 7-offset));
        return bitfield;
    }
    //TODO: test this
    public void writeFilePiece(String fileName, FilePiece piece) {
        try {
            byte[] bytes = piece.getFilePiece();
            FileOutputStream fos = new FileOutputStream(fileName, true);
            fos.write(bytes);
            fos.close();
        }
        //error catching
        catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }
    }
    public void sleep(int timeInSeconds) {
        try {
            TimeUnit.SECONDS.sleep(timeInSeconds);
        } catch (Exception e) {

        }
    }
    public void printMsg(byte[] m, int src, int dest, String srcType, String destType) {

        System.out.println("msg source: " + srcType + " " + src);
        System.out.println("msg dest: " + destType + " " + dest);
        System.out.print("msg as byte array: ");

        for (int i = 0; i < m.length; i++) {
            System.out.printf("0x%x ", m[i]);
        }

        System.out.print("\n");
    }
    public void printBitfieldAsBinaryString(byte[] m) {

        for (byte b : m) {
            System.out.print(Integer.toBinaryString(b) + " ");
        }
        System.out.println();
    }
    public byte[] getInterestedBitfield(byte[] bitfield, byte[] recievedBitfield) {
        byte[] interestedBitfield = new byte[bitfield.length];
        for (int i = 0; i < recievedBitfield.length; i++)
        {
            //bit operations to find what Server has that this client doesn't
            int currentByte = (int)recievedBitfield[i];
            int currentClientByte = (int)bitfield[i];
            currentClientByte = ~currentClientByte;
            int interestedByte = currentClientByte & currentByte;
            interestedBitfield[i] = (byte)interestedByte;
        }
        return  interestedBitfield;
    }
    public void writeFile(String downloadFilePath, FilePiece[] fp, byte[] bf) {
        for (int i = 0; i < bf.length; i++)
        {
            this.writeFilePiece(downloadFilePath, fp[i]); //TODO: test this and make sure the filename is right
        }
    }
}
