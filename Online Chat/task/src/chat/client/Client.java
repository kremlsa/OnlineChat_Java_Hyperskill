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
    public static boolean isRun = true;

    public void go() {

        setUp();

        Thread readerThread = new Thread(new Client.ChatReader());
        Thread writerThread = new Thread(new Client.ChatWriter());
        try {
            readerThread.start();
            writerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            readerThread.join();
            writerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            socket.shutdownInput();
            socket.shutdownOutput();
        } catch (IOException e) {
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
                while (isRun) {
                    String message = input.readUTF();
                    if (message.equals("/exit")) {
                        isRun = false;
                        break;
                    }
                    System.out.println(message);
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    class ChatWriter implements Runnable {
        public void run() {
            while (isRun) {
                try {
                    String message = sc.nextLine();
                    output.writeUTF(message);
                    output.flush();
                    if (message.equals("/exit")) {
                        isRun = false;
                        break;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}