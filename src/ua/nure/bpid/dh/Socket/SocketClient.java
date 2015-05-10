package ua.nure.bpid.dh.Socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Alexander on 08.05.2015.
 */
public abstract class SocketClient {
    public static final String DEFAULT_ADDRESS = "127.0.0.1";
    public static final int DEFAULT_PORT = 6666;

    protected String address;
    protected int port;
    protected volatile boolean connected;
    protected Thread socketListenThread;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    protected ArrayList<MessageReceivedListener> messageReceivedListeners = new ArrayList<>();

    public abstract void start() throws IOException;

    public abstract void stop() throws IOException;

    public abstract void sendMessage(String message) throws IOException;

    public abstract boolean isConnected();

    public abstract String getInfoAboutConnection();

    public void addMessageReceivedListener(MessageReceivedListener listener) {
        messageReceivedListeners.add(listener);
    }

    public void removeMessageReceivedListener(MessageReceivedListener listener) {
        messageReceivedListeners.remove(listener);
    }

    public void removeAllMessageReceivedListeners() {
        messageReceivedListeners.clear();
    }

    protected void notifyMessageReceivedListeners(String message) {
        for (MessageReceivedListener listener : messageReceivedListeners) {
            listener.onMessageReceived(message);
        }
    }
}
