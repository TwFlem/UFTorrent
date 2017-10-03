package com.uftorrent.app.utils;


public class Util {
    public String catStringsFromArrayIntoCSV(String[] arrayOfStrings) {
        String temp = "";

        for (int i =0; i < arrayOfStrings.length; i++) {
            temp = temp + arrayOfStrings[i] + ",";
        }

        return temp;
    }
}
