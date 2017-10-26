package com.uftorrent.app.main;

import com.uftorrent.app.TcpSocket.Client;
import com.uftorrent.app.setup.env.CommonVars;
import com.uftorrent.app.exceptions.InvalidPeerID;
import com.uftorrent.app.setup.env.PeerInfo;
import com.uftorrent.app.utils.Util;
import com.uftorrent.app.protocols.FilePiece;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.lang.*;
import java.util.regex.Pattern;
import java.util.Arrays;

import static java.lang.System.exit;

public class PeerProcess {
    protected static final String workingDir = System.getProperty("user.dir");
    protected static final CommonVars commonVars = new CommonVars();
    protected static final PeerInfo peerInfo = new PeerInfo();
    protected static String peerId;
    protected static String hostName;
    protected static String portNumber;
    protected static String hasCompleteFile;
    protected static String handshakeMessage = "P2PFILESHARINGPROJ0000000000";
    protected static Util util = new Util();
    public static void main(String[] args) {
        clearOldProcessData(); //Deletes log files and peer downloaded files.
        initPeer(args); //Sets package variables regarding this peer.

        //Will make jUnit tests one day
        System.out.println("Here's our env variables!");
        commonVars.print();
        //testing for reading file
        System.out.println("Heres the file reader in action!");
        FilePiece[] filePieces = readFileIntoPiece("Common.cfg", 3);
        for (int i = 0; i < filePieces.length; i++)
        {
            writeFilePiece("testing_log.txt", filePieces[i]);
        }
        System.out.println("Here's all Peer Info!");
        peerInfo.print();

        //Testing get methods
        System.out.format("Number of preferred neighbors: %s%n", commonVars.get("NumberOfPreferredNeighbors"));
        System.out.format("ID's: %s hostName: %s%n", "1004", peerInfo.getHostName("1004"));

        //Display this peer's info
        System.out.println("Here's this Peer's Info!");
        System.out.format("ID: %s HostName: %s, PortNumber: %s, HasCompleteFile: %s%n",
                peerId, hostName, portNumber, hasCompleteFile);

        Client connection = new Client();

        System.out.println("check out the sick logging class too!");
        connection.simulateLogs();
    }

    private static void initPeer(String[] args) {
        try {
            //Initialize this peer Process' info.
            peerId = args[0];

            if (peerId.length() != 4) {
                throw new InvalidPeerID("peer ID must have 4 digits.");
            }

            handshakeMessage = handshakeMessage + peerId;
            hostName = peerInfo.getHostName(peerId);
            portNumber = peerInfo.getPortNumber(peerId);
            hasCompleteFile = peerInfo.getHasCompleteFile(peerId);

        }
        catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Invalid command line arguments. Please pass in a 4 digit PeerProcess ID.");
            exit(1);
        }
        catch (InvalidPeerID ex) {
            System.out.println(ex.getMessage());
            exit(1);
        }
        catch (NullPointerException ex) {
            System.out.println("Invalid peer ID: peer ID provided is not in the PeerProcess list.");
            exit(1);
        }
    }

    private static void clearOldProcessData() {
        try {
            final Pattern logPattern = Pattern.compile("log_peer_\\d{4}\\.log");
            final Pattern directoryPattern = Pattern.compile("peer_\\d{4}");
            File folder = new File(workingDir);
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (logPattern.matcher(file.getName()).matches()
                        || directoryPattern.matcher(file.getName()).matches()) {
                    util.recursiveDelete(file);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: Can't delete old process data.");
        }
    }
    //This method will read a file into an appropriate number of FilePieces, and return an array of these pieces
    private static FilePiece[] readFileIntoPiece(String fileName, int pieceSize){
        try {
            // Use this for reading the data.
            byte[] buffer = new byte[pieceSize];
            int total = 0;
            int nRead = 0;
            int pieceCount = 0;
            int fileLength = Math.toIntExact(new File(fileName).length());
            FilePiece[] pieceArray = new FilePiece[fileLength/pieceSize];
            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            //Debugging code
            /*
            System.out.println("FILE READER INFO HERE");
            System.out.println(new String(data));
            */
            for (int i = 0; i < pieceArray.length; i++)
            {
                byte[] section = new byte[pieceSize];

                pieceArray[i] = new FilePiece(Arrays.copyOfRange(data, i*pieceSize, i*3 + pieceSize), pieceSize);
            }
            //debugging code
            /*
            for (int i = 0; i < pieceArray.length; i++)
            {
                System.out.println(new String(pieceArray[i].getFilePiece()));
            }
            */
            return pieceArray;
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
        return null;
    }
    private static void writeFilePiece(String fileName, FilePiece piece)
    {
        try {
            byte[] bytes = piece.getFilePiece();
            FileOutputStream fos = new FileOutputStream("testing_log.txt", true);
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

