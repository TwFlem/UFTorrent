package com.uftorrent.app.protocols;

public class FilePiece {
    public byte [] filePiece;
    public int pieceIndex;
    public FilePiece() {
        this.filePiece = [];
        this.pieceIndex = 0;
    }
    public FilePiece(byte[] filePiece, int pieceIndex) {
        this.filePiece = filePiece;
        this.pieceIndex = pieceIndex;
    }

    public byte[] getFilePiece() {
        return filePiece;
    }

    public void setFilePiece(byte[] filePiece) {
        this.filePiece = filePiece;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }
}
