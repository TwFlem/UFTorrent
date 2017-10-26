package com.uftorrent.app.protocols;

public class FilePiece {
    public byte [] filePiece;
    public int pieceIndex;

    public FilePiece() {
        this.filePiece = new byte[150];
        this.pieceIndex = 0;
    }

    public FilePiece(byte[] filePiece, int pieceIndex) {
        this.filePiece = filePiece;
        this.pieceIndex = pieceIndex;
    }

    public byte[] getFilePiece() {
        if (this.filePiece != null) {
            return this.filePiece;
        }
        else
        {
            return null;
        }
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
