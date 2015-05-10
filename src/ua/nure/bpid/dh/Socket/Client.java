package ua.nure.bpid.dh.Socket;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Alexander on 06.05.2015.
 */
public class Client extends SocketClient {
    private Socket socket;
    public Client() {
        address = DEFAULT_ADDRESS;
        port = DEFAULT_PORT;
        connected = false;
    }

    public Client(String address, int port) {
        if (address == null || address.length() < 1 || port < 1025)
            throw new IllegalArgumentException("Address must be bigger than 1 symbol and port must be bigger than 1024");
        this.address = address;
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        try {
            socket = new Socket(InetAddress.getByName(address), port);
            connected = true;
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            socketListenThread = new Thread(() -> {
                try {
                    while (connected) {
                        String line = dataInputStream.readUTF();
                        if (line != null) {
                            notifyMessageReceivedListeners(line);
                        }
                    }
                } catch (IOException e) {
                    connected = false;
                    e.printStackTrace();
                }
            });
            socketListenThread.start();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            connected = false;
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
        if (socket != null && !socket.isClosed()) {
            socket.close();
            socket = null;
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
        return connected && socket.isConnected() && dataInputStream != null && dataOutputStream != null;
    }

    @Override
    public String getInfoAboutConnection() {
        if (socket == null) return "Is not connected";
        String localAddress = socket.getLocalSocketAddress().toString();
        String remoteAddress = socket.getRemoteSocketAddress().toString();
        return "Local Address: " + localAddress + "\nRemote address: " + remoteAddress;
    }
}
