package chat.server;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Server {
    ArrayList clients;
    public static Scanner sc = new Scanner(System.in);
    Socket socket;

    public class ClientHandler implements Runnable {
        DataInputStream input;
        DataOutputStream output;
        Socket sock;

        public ClientHandler(Socket clientSocket) {
            try {
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream()
                sock = clientSocket;

            } catch (Exception ex) { ex.printStackTrace(); }
        }

        public void run() {
            String message;
            try {
                while (true) {
                    message = input.readUTF();
                    if (message.equals("/exit")) {
                        System.out.println("Client 1 disconnected!");
                        break;
                    } else {
                        System.out.println("Client sent: " + message);
                        System.out.println(answer());
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        public String answer() {
            return "Sent to client 2: Count is 7";

        }
    }

    public void go() {

        setUp();

        Thread readerThread = new Thread(new Server.ChatReader());
        Thread writerThread = new Thread(new Server.ChatWriter());
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
            String address = "127.0.0.1";
            int port = 23456;
            ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address));
            Socket socket = server.accept();
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            System.out.println("Server started!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server().go();
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
