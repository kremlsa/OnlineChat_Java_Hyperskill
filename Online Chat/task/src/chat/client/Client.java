package chat.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {
    public static Scanner sc = new Scanner(System.in);
    DataInputStream input;
    DataOutputStream output;
    Socket socket;

    public void go() {

        setUp();

        Thread readerThread = new Thread(new Client.ChatReader());
        Thread writerThread = new Thread(new Client.ChatWriter());
        readerThread.start();
        writerThread.start();
        try {
            readerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            writerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void setUp() {
        try {
            socket = new Socket("127.0.0.1", 23456);
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            System.out.println("Client started!");
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client().go();
    }
    class ChatReader implements Runnable {
        public void run() {
            try {
                for (long stop = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); stop > System.nanoTime(); ) {
                    System.out.println(input.readUTF());
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    class ChatWriter implements Runnable {
        public void run() {
            for (long stop = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); stop > System.nanoTime(); ) {
                try {
                    output.writeUTF(sc.nextLine());
                    output.flush();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}