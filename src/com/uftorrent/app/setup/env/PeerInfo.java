package com.uftorrent.app.setup.env;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.FileReader;
import java.io.BufferedReader;

public class PeerInfo {
    private Map<String, String[]> peerInfo = new HashMap<>();
    public PeerInfo() {
        String fileName = "PeerInfo.cfg";
        loadPeerInfo(fileName);
    }

    private void loadPeerInfo(String fileName) {
        String line;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                // The first element of this array is the peer's ID
                String[] peerInfoIDKeyAndValues = line.split("=");
                String[] peerInfoValues = peerInfoIDKeyAndValues[1].split(",");
                this.peerInfo.put(peerInfoIDKeyAndValues[0], peerInfoValues);
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }
    }

    public void print() {
        for (String peerID : this.peerInfo.keySet()) {
            System.out.format("ID: %s: HostName: %s PortNumber: %s HasCompleteFile: %s%n",
                    peerID, this.peerInfo.get(peerID)[0], this.peerInfo.get(peerID)[1], this.peerInfo.get(peerID)[2]);
        }
    }
    public String getHostName(String id) {
        return this.peerInfo.get(id)[0];
    }
    public String getPortNumber(String id) {
        return this.peerInfo.get(id)[1];
    }
    public String getHasCompleteFile(String id) {
        return this.peerInfo.get(id)[2];
    }
}
