package com.uftorrent.app.exceptions;

public class InvalidPeerID extends Exception {
    private static String TAG = "Invalid peer ID: ";
    public InvalidPeerID(String message) {
        super(TAG + message);
    }
}
