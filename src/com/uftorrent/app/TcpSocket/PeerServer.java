package com.uftorrent.app.TcpSocket;


import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer implements Runnable {
    public void run() {
        System.out.println("Hello from a server thread!");
        String sendToClientData = "Hello from the Server!";
        try {
            ServerSocket srvr = new ServerSocket(1234);

            // After listening on a port
            makeClient();

            // Wait for anything trying to make a connection
            Socket skt = srvr.accept();
            System.out.print("A client has contacted me!\n");

            // Object use to write data to a socket
            PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
            System.out.print("Sending string to client: " + sendToClientData + "\n");

            // Shove data out into the network through the socket.
            out.print(sendToClientData);

            // Server cleanup procedure
            out.close();
            skt.close();
            srvr.close();
        }
        catch(Exception e) {
            System.out.print("Whoops! The Server quit unexpectedly!\n");
        }
    }
    private void makeClient() {
        Thread peerClient = new Thread(new PeerClient());
        peerClient.start();
    }
}
