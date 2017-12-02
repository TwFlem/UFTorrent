package com.uftorrent.app.setup.env;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.FileReader;
import java.io.BufferedReader;

public class CommonVars {
    private long numberOfPrefferedNeighbors;
    private double optimisticUnchokingInterval;
    private long pieceSize;
    private String fileName;
    private double unchokingInterval;
    private long fileSize;
    private int numberOfPieces;
    private long lastPieceSize;
    public CommonVars() {
        String fileName = "Common.cfg";
        loadEnvVars(fileName);
    }

    private void loadEnvVars(String fileName) {
        Map<String, String> envVars = new HashMap<>();
        String line;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                String[] envKeyValue = line.split(" ");
                envVars.put(envKeyValue[0], envKeyValue[1]);
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }
        this.setFields(envVars);
    }

    private void setFields(Map<String, String> envVars) {
        this.numberOfPrefferedNeighbors = Long.parseLong(envVars.get("NumberOfPreferredNeighbors"));
        this.optimisticUnchokingInterval = Double.parseDouble(envVars.get("OptimisticUnchokingInterval"));
        this.pieceSize = Long.parseLong(envVars.get("PieceSize"));
        this.fileName = envVars.get("FileName");
        this.unchokingInterval = Double.parseDouble(envVars.get("UnchokingInterval"));
        this.fileSize = Long.parseLong(envVars.get("FileSize"));
        this.numberOfPieces = (int)(getFileSize()/this.pieceSize + 1);
        this.lastPieceSize = getFileSize() - (getNumberOfPieces() - 1) * this.pieceSize;
    }
    public void print() {
        System.out.println("numberOfPrefferedNeighbors " + this.numberOfPrefferedNeighbors );
        System.out.println("optomisticUnchokingInterval " + this.optimisticUnchokingInterval );
        System.out.println("pieceSize " + this.pieceSize );
        System.out.println("fileName " + this.fileName );
        System.out.println("unchokingInterval " + this.unchokingInterval );
        System.out.println("fileSize " + this.fileSize );
        System.out.println("numberOfPieces " + this.numberOfPieces );
        System.out.println("lastPieceSize " + this.lastPieceSize );
    }

    public long getNumberOfPrefferedNeighbors() {
        return numberOfPrefferedNeighbors;
    }

    public double getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public long getPieceSize() {
        return pieceSize;
    }

    public String getFileName() {
        return fileName;
    }

    public double getUnchokingInterval() {
        return unchokingInterval;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getNumberOfPieces() {
        return numberOfPieces;
    }

    public long getLastPieceSize() {
        return lastPieceSize;
    }
}
