package com.uftorrent.app.main;

import com.uftorrent.app.TcpSocket.PeerClient;
import com.uftorrent.app.TcpSocket.PeerServer;
import com.uftorrent.app.TcpSocket.UFTorrentServerProtocol;
import com.uftorrent.app.setup.env.CommonVars;
import com.uftorrent.app.exceptions.InvalidPeerID;
import com.uftorrent.app.setup.env.PeerInfo;
import com.uftorrent.app.utils.Util;
import com.uftorrent.app.protocols.FilePiece;
import com.uftorrent.app.protocols.Message;

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
    protected static int peerId;
    protected static String hostName;
    protected static int portNumber;
    protected static boolean hasCompleteFile;
    protected static String handshakeMessage = "P2PFILESHARINGPROJ0000000000";
    protected static String downloadFilePath;
    protected static byte[] bitfield;
    protected static byte[] fullBitfield;
    protected static byte[] emptyBitfiled;
    protected static FilePiece[] pieces; //keep track of what File pieces I have
    protected static Util util = new Util();
    public static void main(String[] args) {
        clearOldProcessData(); //Deletes log files and peer downloaded files.
        initPeer(args); //Sets package variables regarding this peer.
        //TESTING ---------
        int z = 3322;
        byte[] testUtil = util.intToByteArray(z);
        int backToInt = util.packetSize(testUtil);
        for (int i = 0; i < 4; i++)
        {
            System.out.print(testUtil[i] + " ");
        }
        System.out.println("BackToInt: " + backToInt);
        byte[] testBitfield = new byte[bitfield.length];
        for (int i = 0; i < bitfield.length; i++)
        {
            testBitfield[i] = (byte)0xBC;
        }
        testBitfield[5] = (byte)0x55;
        testBitfield[6] = (byte)0x44;
        bitfield[0] = (byte)0x1A;
        bitfield[1] = (byte)0xAA;
        bitfield[2] = (byte)0xAA;
        bitfield[5] = (byte)0xAA;
        bitfield[6] = (byte)0xAA;
        bitfield[7] = (byte)0xAA;
        bitfield[8] = (byte)0xAA;
        byte[] testPieceIndex = new byte[4];
        testPieceIndex[0] = 0;
        testPieceIndex[1] = 0;
        testPieceIndex[2] = 0;
        testPieceIndex[3] = 4;
        Message haveTest = new Message(5, (byte)0x4, testPieceIndex);
        byte[] haveTestArray = haveTest.msgToByteArray();
        Message joe = new UFTorrentServerProtocol(5).handleInput((byte)0x4, haveTestArray);
        Message frank = new UFTorrentServerProtocol(5).handleInput((byte)0x5, testBitfield);
        for (int i = 0; i < bitfield.length; i++) {
            System.out.print(frank.getData()[i]);
            System.out.print(" ");
        }
        System.out.println("");
        System.out.println("Printing joe Message type now");
        System.out.println(joe.getMessageType());
        //Will make jUnit tests one day
        System.out.println("Here's our env variables!");
        commonVars.print();
        //testing for reading file
        System.out.println("Heres the file reader in action!");
        FilePiece[] filePieces = readFileIntoPiece("Common.cfg", 3);
        for (int i = 0; i < filePieces.length; i++) {
            writeFilePiece(downloadFilePath, filePieces[i]);
        }
        System.out.println("Here's all Peer Info!");
        peerInfo.print();

        //Display this peer's info
        System.out.println("Here's this Peer's Info!");
        System.out.format("ID: %s HostName: %s, PortNumber: %s, HasCompleteFile: %s%n",
                peerId, hostName, portNumber, hasCompleteFile);


        // Start the Server thread
        Thread peerServer = new Thread(new PeerServer());
        peerServer.start();

        // Start the Client thread
        Thread peerClient = new Thread(new PeerClient());
        peerClient.start();

        // wait for the sever thread to finish
        try {
            peerServer.join();
        } catch (Exception e) {
            System.out.println("Thread execution failed");
        }
    }

    private static void initPeer(String[] args) {
        try {
            //Initialize this peer Process' info.
            try {
                peerId = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("Peer id must be a 4 digit integer");
            }

            if (peerId < 1000) {
                throw new InvalidPeerID("peer ID must have 4 digits.");
            }

            handshakeMessage = handshakeMessage + peerId;
            hostName = peerInfo.getHostName(peerId);
            portNumber = peerInfo.getPortNumber(peerId);
            hasCompleteFile = peerInfo.getHasCompleteFile(peerId);
            int sizeOfBitfield = commonVars.getNumberOfPieces()/8 + 1;
            int numOfBitsForLastPiece = commonVars.getNumberOfPieces() - (sizeOfBitfield - 1) * 8;
            bitfield = new byte[sizeOfBitfield];
            emptyBitfiled = new byte[sizeOfBitfield];
            fullBitfield = new byte[sizeOfBitfield];
            pieces = new FilePiece[commonVars.getNumberOfPieces()];
            System.out.println("Size of bitfield: " + bitfield.length);

            if (hasCompleteFile) {
               for (int i = 0; i < bitfield.length - 1; i++) {
                   bitfield[i] = -1;
               }
               bitfield[bitfield.length -1] = util.intToBigEndianBitChunk(numOfBitsForLastPiece);
            } else {
                for (int i = 0; i < bitfield.length - 1; i++) {
                    bitfield[i] = 0x00;
                }
            }

            for (int i = 0; i < bitfield.length; i++) {
                fullBitfield[i] = -1;
            }

            for (int i = 0; i < emptyBitfiled.length; i++) {
                fullBitfield[i] = 0x00;
            }

            // Create downloading Directory
            File downloadDir = new File("peer_" + peerId);
            System.out.println("creating directory: " + downloadDir.getName());
            boolean createDirSuccessful = downloadDir.mkdir();

            if (!createDirSuccessful) {
                throw new SecurityException();
            }
            System.out.println(downloadDir.getPath());
            downloadFilePath = downloadDir.getPath() + "/downloadFile";

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
        catch(SecurityException ex) {
            System.out.println("There was a problem creating the download directory. Please remove any past downloading directories.");
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
    private static FilePiece[] readFileIntoPiece(String fileName, int pieceSize) {
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
            for (int i = 0; i < pieceArray.length; i++) {
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

