package com.kostenko.dh.gui;

import com.kostenko.dh.Socket.Server;
import com.kostenko.dh.Socket.SocketClient;
import com.kostenko.dh.Encryption.DiffieHellmanGenerator;
import com.kostenko.dh.Encryption.Encrypting;
import com.kostenko.dh.Socket.Client;
import com.kostenko.dh.Socket.MessageReceivedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
        comboBoxAppType.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                appType = comboBoxAppType.getSelectedItem().toString();
            }
        });
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
        buttonCreateConnection.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createConnection();
            }
        });
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
        buttonGenerateKeys.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textFieldPublicFriendKey.getText().length() < 1) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Please, enter public key...");
                    return;
                }
                int key = 0;
                try {
                    key = Integer.parseInt(textFieldPublicFriendKey.getText());
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Please, enter CORRECT public key...");
                    return;
                }
                diffieHellmanGenerator.createSecretKey(key);
                textFieldSecretKey.setText(diffieHellmanGenerator.getSecretKey() + "");
                buttonGenerateKeys.setEnabled(false);
                textFieldPublicFriendKey.setEditable(false);
            }
        });
    }

    private void createClient(int socketPort) {
        socketClient = new Client(textFieldAddress.getText(), socketPort);
        textFieldNumberP.setEditable(true);
        textFieldNumberG.setEditable(true);
        textFieldPublicFriendKey.setEditable(true);
        buttonGenerateKeys.setEnabled(true);
        buttonGenerateKeys.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textFieldPublicFriendKey.getText().length() < 1 && textFieldNumberP.getText().length() < 1
                        && textFieldNumberG.getText().length() < 1) {
                    JOptionPane.showMessageDialog(MainWindow.this, "Please, enter all public keys...");
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
                    JOptionPane.showMessageDialog(MainWindow.this, "Please, enter CORRECT all public keys...");
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
            }
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
            socketClient.addMessageReceivedListener(new MessageReceivedListener() {
                @Override
                public void onMessageReceived(String message) {
                    String text = textAreaMessages.getText();
                    String decryption = Encrypting.decrypt(message, textFieldSecretKey.getText());
                    textAreaMessages.setText(text + "\nSomeBody: " + message + "\nDecryption: " + decryption + "\n");
                }
            });
            textFieldUserMessage.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage(textFieldUserMessage.getText());
                }
            });
            buttonSendMessage.setEnabled(true);
            buttonSendMessage.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage(textFieldUserMessage.getText());
                }
            });
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainJPanel = new JPanel();
        mainJPanel.setLayout(new GridBagLayout());
        mainJPanel.setMaximumSize(new Dimension(1920, 1080));
        mainJPanel.setMinimumSize(new Dimension(750, 480));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainJPanel.add(panel1, gbc);
        comboBoxAppType = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(comboBoxAppType, gbc);
        textFieldAddress = new JTextField();
        textFieldAddress.setText("127.0.0.1");
        textFieldAddress.setToolTipText("ip address");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(textFieldAddress, gbc);
        textFieldPort = new JTextField();
        textFieldPort.setText("6666");
        textFieldPort.setToolTipText("port");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(textFieldPort, gbc);
        buttonCreateConnection = new JButton();
        buttonCreateConnection.setText("Create Connection");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(buttonCreateConnection, gbc);
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("App Type");
        label1.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel1.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setHorizontalAlignment(0);
        label2.setHorizontalTextPosition(0);
        label2.setText("IP Address");
        label2.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel1.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setHorizontalAlignment(0);
        label3.setHorizontalTextPosition(0);
        label3.setText("Port");
        label3.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel1.add(label3, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainJPanel.add(panel2, gbc);
        textFieldNumberP = new JTextField();
        textFieldNumberP.setMinimumSize(new Dimension(200, 24));
        textFieldNumberP.setToolTipText("public number P");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(textFieldNumberP, gbc);
        textFieldNumberG = new JTextField();
        textFieldNumberG.setMinimumSize(new Dimension(200, 24));
        textFieldNumberG.setToolTipText("public number G");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(textFieldNumberG, gbc);
        textFieldSecretKey = new JTextField();
        textFieldSecretKey.setMinimumSize(new Dimension(200, 24));
        textFieldSecretKey.setText("");
        textFieldSecretKey.setToolTipText("secret key");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(textFieldSecretKey, gbc);
        textFieldPrivateNumberA = new JTextField();
        textFieldPrivateNumberA.setMinimumSize(new Dimension(200, 24));
        textFieldPrivateNumberA.setToolTipText("private number A");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(textFieldPrivateNumberA, gbc);
        textFieldPublicSelfKey = new JTextField();
        textFieldPublicSelfKey.setMinimumSize(new Dimension(200, 24));
        textFieldPublicSelfKey.setToolTipText("public self Key");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(textFieldPublicSelfKey, gbc);
        textFieldPublicFriendKey = new JTextField();
        textFieldPublicFriendKey.setMinimumSize(new Dimension(200, 24));
        textFieldPublicFriendKey.setToolTipText("public partner key");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(textFieldPublicFriendKey, gbc);
        buttonGenerateKeys = new JButton();
        buttonGenerateKeys.setText("Generate Keys");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(buttonGenerateKeys, gbc);
        final JLabel label4 = new JLabel();
        label4.setHorizontalAlignment(0);
        label4.setHorizontalTextPosition(0);
        label4.setText("Public number  P");
        label4.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel2.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setHorizontalAlignment(0);
        label5.setHorizontalTextPosition(0);
        label5.setText("Public number G");
        label5.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel2.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setHorizontalAlignment(0);
        label6.setHorizontalTextPosition(0);
        label6.setText("Public Friend Key");
        label6.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel2.add(label6, gbc);
        final JLabel label7 = new JLabel();
        label7.setHorizontalAlignment(0);
        label7.setHorizontalTextPosition(0);
        label7.setText("Public Self Key");
        label7.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        panel2.add(label7, gbc);
        final JLabel label8 = new JLabel();
        label8.setHorizontalAlignment(0);
        label8.setHorizontalTextPosition(0);
        label8.setText("Private number A");
        label8.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        panel2.add(label8, gbc);
        final JLabel label9 = new JLabel();
        label9.setHorizontalAlignment(0);
        label9.setHorizontalTextPosition(0);
        label9.setText("Secret Key");
        label9.setVerticalAlignment(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        panel2.add(label9, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setAutoscrolls(true);
        scrollPane1.setDoubleBuffered(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainJPanel.add(scrollPane1, gbc);
        textAreaMessages = new JTextArea();
        textAreaMessages.setLineWrap(true);
        scrollPane1.setViewportView(textAreaMessages);
        textFieldUserMessage = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainJPanel.add(textFieldUserMessage, gbc);
        buttonSendMessage = new JButton();
        buttonSendMessage.setText("Send");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainJPanel.add(buttonSendMessage, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainJPanel;
    }
}
