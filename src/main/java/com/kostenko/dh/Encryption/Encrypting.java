package com.kostenko.dh.Encryption;

/**
 * Created by Alexander on 09.05.2015.
 * Vigenere encryption.
 */
public class Encrypting {

    public static final int NUMBER_OF_ALPHABET_SYMBOLS = 256;

    public static String encrypt(String text, String key) {
        String newKey = generateKey(text, key);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char pi = text.charAt(i);
            char ki = newKey.charAt(i);
            char ci = (char) ((pi + ki) % NUMBER_OF_ALPHABET_SYMBOLS);
            sb.append(ci);
        }
        return sb.toString();
    }

    public static String decrypt(String cipher, String key) {
        String newKey = generateKey(cipher, key);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cipher.length(); i++) {
            char ci = cipher.charAt(i);
            char ki = newKey.charAt(i);
            char pi = (char) ((ci - ki + NUMBER_OF_ALPHABET_SYMBOLS) % NUMBER_OF_ALPHABET_SYMBOLS);
            sb.append(pi);
        }
        return sb.toString();
    }

    private static String generateKey(String text, String key) {
        int size = text.length();
        if (key.length() == size)
            return key;
        StringBuilder sb = new StringBuilder();
        char[] keySymbols = key.toCharArray();
        while (sb.length() < size) {
            for (char symbol : keySymbols) {
                if (sb.length() >= size)
                    break;
                sb.append(symbol);
            }
        }
        return sb.toString();
    }
}