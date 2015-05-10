package ua.nure.bpid.dh.gui;

import ua.nure.bpid.dh.Encryption.DiffieHellmanGenerator;
import ua.nure.bpid.dh.Encryption.Encrypting;
import ua.nure.bpid.dh.Socket.Client;
import ua.nure.bpid.dh.Socket.Server;
import ua.nure.bpid.dh.Socket.SocketClient;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Created by Alexander on 08.05.2015.
 */
public class MainWindow extends JFrame {
    private JPanel mainJPanel;
    private JTextArea textAreaMessages;
    private JTextField textFieldUserMessage;
    private JButton buttonSendMessage;
    private JComboBox comboBoxAppType;
    private JTextField textFieldAddress;
    private JTextField textFieldPort;
    private JButton buttonCreateConnection;
    private JTextField textFieldNumberP;
    private JTextField textFieldNumberG;
    private JTextField textFieldPublicFriendKey;
    private JButton buttonGenerateKeys;
    private JTextField textFieldSecretKey;
    private JTextField textFieldPrivateNumberA;
    private JTextField textFieldPublicSelfKey;

    private SocketClient socketClient;
    private DiffieHellmanGenerator diffieHellmanGenerator;
    private String appType;

    public MainWindow() {
        super();
        setContentPane(mainJPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(mainJPanel.getMinimumSize());
        setVisible(true);
        setListeners();
        comboBoxAppType.addItem("Server");
        comboBoxAppType.addItem("Client");
        textAreaMessages.setEditable(false);
        textAreaMessages.setAutoscrolls(true);
        buttonGenerateKeys.setEnabled(false);
        buttonSendMessage.setEnabled(false);
        textFieldPublicSelfKey.setEditable(false);
        textFieldNumberP.setEditable(false);
        textFieldNumberG.setEditable(false);
        textFieldPublicFriendKey.setEditable(false);
        textFieldUserMessage.setEditable(false);
        textFieldPrivateNumberA.setEditable(false);
        textFieldSecretKey.setEditable(false);
        appType = comboBoxAppType.getSelectedItem().toString();
    }

    private void setListeners() {
        comboBoxAppType.addActionListener(e -> appType = comboBoxAppType.getSelectedItem().toString());
        textFieldAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                checkAbilityToConnection();
            }
        });
        textFieldPort.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                checkAbilityToConnection();
            }
        });
        buttonCreateConnection.addActionListener(e -> createConnection());
        addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (socketClient != null && socketClient.isConnected()) {
                    try {
                        socketClient.stop();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(MainWindow.this, ex.getMessage());
                    }
                }
            }
        });
    }

    private void createConnection() {
        int socketPort = 0;
        try {
            socketPort = Integer.parseInt(textFieldPort.getText());
            if (socketPort < 1024)
                throw new IllegalArgumentException("Port is not available");
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Port is not correct");
            return;
        }
        if (appType.equals("Server")) {
            createServer(socketPort);
        } else {
            createClient(socketPort);
        }
        startSocketClient();
    }

    private void createServer(int socketPort) {
        socketClient = new Server(textFieldAddress.getText(), socketPort);
        diffieHellmanGenerator = new DiffieHellmanGenerator();
        textFieldPublicSelfKey.setText(diffieHellmanGenerator.getPublicKey() + "");
        textFieldPrivateNumberA.setText(diffieHellmanGenerator.getA() + "");
        textFieldNumberP.setText(diffieHellmanGenerator.getP() + "");
        textFieldNumberG.setText(diffieHellmanGenerator.getG() + "");
        textFieldPublicFriendKey.setEditable(true);
        buttonGenerateKeys.setEnabled(true);
        buttonGenerateKeys.addActionListener(e -> {
            if (textFieldPublicFriendKey.getText().length() < 1) {
                JOptionPane.showMessageDialog(this, "Please, enter public key...");
                return;
            }
            int key = 0;
            try {
                key = Integer.parseInt(textFieldPublicFriendKey.getText());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Please, enter CORRECT public key...");
                return;
            }
            diffieHellmanGenerator.createSecretKey(key);
            textFieldSecretKey.setText(diffieHellmanGenerator.getSecretKey() + "");
            buttonGenerateKeys.setEnabled(false);
            textFieldPublicFriendKey.setEditable(false);
        });
    }

    private void createClient(int socketPort) {
        socketClient = new Client(textFieldAddress.getText(), socketPort);
        textFieldNumberP.setEditable(true);
        textFieldNumberG.setEditable(true);
        textFieldPublicFriendKey.setEditable(true);
        buttonGenerateKeys.setEnabled(true);
        buttonGenerateKeys.addActionListener(e -> {
            if (textFieldPublicFriendKey.getText().length() < 1 && textFieldNumberP.getText().length() < 1
                    && textFieldNumberG.getText().length() < 1) {
                JOptionPane.showMessageDialog(this, "Please, enter all public keys...");
                return;
            }
            int pNumber = 0;
            int gNumber = 0;
            int publicKey = 0;
            try {
                pNumber = Integer.parseInt(textFieldNumberP.getText());
                gNumber = Integer.parseInt(textFieldNumberG.getText());
                publicKey = Integer.parseInt(textFieldPublicFriendKey.getText());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Please, enter CORRECT all public keys...");
                return;
            }
            diffieHellmanGenerator = new DiffieHellmanGenerator(pNumber, gNumber);
            diffieHellmanGenerator.createSecretKey(publicKey);
            textFieldPublicSelfKey.setText(diffieHellmanGenerator.getPublicKey() + "");
            textFieldSecretKey.setText(diffieHellmanGenerator.getSecretKey() + "");
            textFieldPrivateNumberA.setText(diffieHellmanGenerator.getA() + "");
            buttonGenerateKeys.setEnabled(false);
            textFieldNumberP.setEditable(false);
            textFieldNumberG.setEditable(false);
            textFieldPublicFriendKey.setEditable(false);
        });
    }

    private void startSocketClient() {
        if (socketClient != null) {
            try {
                socketClient.start();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                return;
            }
            comboBoxAppType.setEnabled(false);
            textFieldAddress.setEditable(false);
            textFieldPort.setEditable(false);
            buttonCreateConnection.setEnabled(false);
            textFieldUserMessage.setEditable(true);
            socketClient.addMessageReceivedListener(e -> {
                String text = textAreaMessages.getText();
                String decryption = Encrypting.decrypt(e, textFieldSecretKey.getText());
                textAreaMessages.setText(text + "\nSomeBody: " + e + "\nDecryption: " + decryption + "\n");
            });
            textFieldUserMessage.addActionListener(e -> sendMessage(textFieldUserMessage.getText()));
            buttonSendMessage.setEnabled(true);
            buttonSendMessage.addActionListener(e -> sendMessage(textFieldUserMessage.getText()));
        }
    }

    private void sendMessage(String text) {
        if (socketClient != null) {
            String message = Encrypting.encrypt(text, textFieldSecretKey.getText());
            try {
                socketClient.sendMessage(message);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
                return;
            }
            String allText = textAreaMessages.getText();
            textAreaMessages.setText(allText + "\nI'm: " + text + "\n");
            textFieldUserMessage.setText("");
        }
    }

    private void checkAbilityToConnection() {
        if (textFieldPort.getText() != null && textFieldAddress.getText() != null) {
            buttonCreateConnection.setEnabled(true);
        } else {
            buttonCreateConnection.setEnabled(false);
        }
    }
}
