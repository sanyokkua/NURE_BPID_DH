package ua.nure.bpid.dh.Socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Alexander on 06.05.2015.
 */
public class Server extends SocketClient {
    private ServerSocket serverSocket;
    private Socket socketForClient;

    public Server() {
        address = DEFAULT_ADDRESS;
        port = DEFAULT_PORT;
        connected = false;
    }

    public Server(String address, int port) {
        if (address == null || address.length() < 1 || port < 1025)
            throw new IllegalArgumentException("Address must be bigger than 1 symbol and port must be bigger than 1024");
        this.address = address;
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        try {
            serverSocket = new ServerSocket(port, 0, InetAddress.getByName(address));
            socketListenThread = new Thread(() -> {
                try {
                    socketForClient = serverSocket.accept();
                    dataInputStream = new DataInputStream(socketForClient.getInputStream());
                    dataOutputStream = new DataOutputStream(socketForClient.getOutputStream());
                    connected = true;
                    while (connected) {
                        String line = dataInputStream.readUTF();
                        if (line != null) {
                            notifyMessageReceivedListeners(line);
                        }
                    }
                } catch (IOException ex) {
                    connected = false;
                    ex.printStackTrace();
                }
            });
            socketListenThread.start();
        } catch (IOException ex) {
            connected = false;
            System.out.println(ex.getMessage());
            throw ex;
        }
    }


    @Override
    public void stop() throws IOException {
        connected = false;
        if (dataInputStream != null) {
            dataInputStream.close();
            dataInputStream = null;
        }
        if (dataOutputStream != null) {
            dataOutputStream.close();
            dataOutputStream = null;
        }
        if (socketForClient != null && !socketForClient.isClosed()) {
            socketForClient.close();
            socketForClient = null;
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            serverSocket = null;
        }
    }

    @Override
    public void sendMessage(String message) throws IOException {
        if (!connected)
            throw new IOException("Connection is corrupt");
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();
    }

    @Override
    public boolean isConnected() {
        return connected && socketForClient.isConnected() && dataInputStream != null && dataOutputStream != null;
    }

    @Override
    public String getInfoAboutConnection() {
        if (serverSocket == null)
            return "Is not connected";
        String localAddress = serverSocket.getLocalSocketAddress().toString();
        return "Local Address: " + localAddress;
    }
}
