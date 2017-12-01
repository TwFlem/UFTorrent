package com.uftorrent.app.main;

import com.uftorrent.app.TcpSocket.ClientConnectionHandler;
import com.uftorrent.app.TcpSocket.PeerClient;
import com.uftorrent.app.TcpSocket.PeerServer;
import com.uftorrent.app.TcpSocket.ServerConnectionHandler;
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
import java.util.HashMap;
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
    protected static String handshakeMessage = "P2PFILESHARINGPROJ0000000000";
    protected static String downloadFilePath;
    protected static byte[] bitfield;
    protected static byte[] fullBitfield;
    protected static byte[] emptyBitfield;
    protected static HashMap<Integer, ClientConnectionHandler> clientConnectionHandlers = new HashMap<>();
    protected static HashMap<Integer, ServerConnectionHandler> serverConnectionHandlers = new HashMap<>();
    protected static FilePiece[] pieces; //keep track of what File pieces I have
    protected static Util util = new Util();
    public static void main(String[] args) {
        clearOldProcessData(); //Deletes log files and peer downloaded files.
        initPeer(args); //Sets package variables regarding this peer.
        //Will make jUnit tests one day
        System.out.println("Here's our env variables!");
        commonVars.print();
        //testing for reading file
        System.out.println("Heres the file reader in action!");

        if (peerInfo.getHasCompleteFile(peerId)) {
            FilePiece[] filePieces = readFileIntoPiece("test.txt", (int) commonVars.getPieceSize());
            pieces = filePieces;
            for (int i = 0; i < filePieces.length; i++) {
                writeFilePiece(downloadFilePath, filePieces[i]);
            }
        }
        System.out.println("Here's all Peer Info!");
        peerInfo.print();

        //Display this peer's info
        System.out.println("Here's this Peer's Info!");
        System.out.format("ID: %s HostName: %s, PortNumber: %s, HasCompleteFile: %s%n",
                peerId, hostName, portNumber, peerInfo.getHasCompleteFile(peerId));


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
            int sizeOfBitfield = (commonVars.getNumberOfPieces() / 8) + 1;
            int remainderbits = commonVars.getNumberOfPieces() % 8;
            bitfield = new byte[sizeOfBitfield];
            emptyBitfield = new byte[sizeOfBitfield];
            fullBitfield = new byte[sizeOfBitfield];
            pieces = new FilePiece[commonVars.getNumberOfPieces()];
            System.out.println("Size of bitfield: " + bitfield.length);
            System.out.println("last chunk to bits: " + util.intToBigEndianBitChunk(remainderbits));

           for (int i = 0; i < fullBitfield.length - 1; i++) {
               fullBitfield[i] = -1;
           }
           fullBitfield[fullBitfield.length - 1] = util.intToBigEndianBitChunk(remainderbits);

            for (int i = 0; i < emptyBitfield.length; i++) {
                emptyBitfield[i] = 0x00;
            }

            if (peerInfo.getHasCompleteFile(peerId)) {
                bitfield = fullBitfield;
            } else {
                bitfield = emptyBitfield;
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
            FilePiece[] pieceArray = new FilePiece[commonVars.getNumberOfPieces()];
            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            int newStartIndex;
            for (int i = 0; i < pieceArray.length - 1; i++) {
                newStartIndex = i * pieceSize;
                pieceArray[i] = new FilePiece(Arrays.copyOfRange(data, newStartIndex, newStartIndex + pieceSize), pieceSize);
            }
            int lastPieceIndex = pieceArray.length - 1;
            int lastPieceStartIndex = lastPieceIndex * pieceSize;
            pieceArray[lastPieceIndex] =
                    new FilePiece(Arrays.copyOfRange(data, lastPieceStartIndex, lastPieceStartIndex + pieceSize), pieceSize);
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

