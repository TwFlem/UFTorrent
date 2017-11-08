package com.uftorrent.app.utils;


import java.io.File;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

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
        int size = 0;
        for (int i = 0; i < arr.length; i++) {
            size = size + arr[arr.length - i - 1] * (int)Math.pow(16, i);
        }
        return size;
    }
    public byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
}
