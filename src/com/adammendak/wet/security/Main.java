package com.adammendak.wet.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.Key;
import java.security.SecureRandom;

public class Main {

    public static void main(String[] args) throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecureRandom random = new SecureRandom();
        keygen.init(random);
        SecretKey key = keygen.generateKey();

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("hash.txt"))) {
            out.writeObject(key);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 5; i++) {
            final int finalI = i;
            Thread t = new Thread(()-> {
                System.out.println("Thread nr" + finalI);
                encrypt(finalI);
                decrypt(finalI);
            });
            t.start();
        }
    }

    static synchronized void encrypt(int counter) {
        int mode = Cipher.ENCRYPT_MODE;
        try (ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream("hash.txt"));
                 InputStream in = new FileInputStream("input.txt");
                 OutputStream out = new FileOutputStream("output.txt")) {

            Key keyEnc = (Key) keyIn.readObject();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, keyEnc);
            Util.crypt(in, out, cipher);
        } catch (Exception e) {
            System.err.println("Error during encryption");
            e.getStackTrace();
            return;
        }
        printNameAndCounter("Encrypted thread= %s", counter);
    }

    static synchronized void decrypt(int counter) {
        int mode = Cipher.DECRYPT_MODE;

        try (ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream("hash.txt"));
                 InputStream in = new FileInputStream("output.txt");
                 OutputStream out = new FileOutputStream("decrypted.txt")) {

            Key keyDec = (Key) keyIn.readObject();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, keyDec);
            Util.crypt(in, out, cipher);
        } catch (Exception e) {
            System.err.println("Error during decryption");
            e.getStackTrace();
            return;
        }
        printNameAndCounter("Decrypted thread= %s", counter);
    }

    static void printNameAndCounter(String name, int counter) {
        System.out.println(String.format(name, String.valueOf(counter)));
    }

}
