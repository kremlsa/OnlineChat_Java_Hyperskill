package chat.server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {
    Map<String, Socket> clientSockets = Collections.synchronizedMap(new HashMap<String, Socket>());

    static ServerBack backend;

    public class ClientHandler implements Runnable {
        DataInputStream input;
        DataOutputStream output;
        Socket sock;
        String name;
        int clientNumber;

        public ClientHandler(Socket clientSocket, int number) {
            try {
                clientNumber = number;
                sock = clientSocket;
                input = new DataInputStream(sock.getInputStream());
                output = new DataOutputStream(sock.getOutputStream());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void run() {
            try {
                boolean isRun = true;
                output.writeUTF("Server: authorize or register");
                output.flush();
                while (isRun) {
                    String command = input.readUTF();
                    switch (command.split(" ")[0]) {
                        case "/exit":
                            output.writeUTF("/exit");
                            output.flush();
                            sock.close();
                            System.out.println("Client " + clientNumber + " disconnected!");
                            backend.clientOffline(name);
                            clientSockets.remove(name);
                            isRun = false;
                            break;
                        case "/auth":
                            name = backend.auth(input, output, command);
                            clientSockets.put(name, sock);
                            break;
                        case "/kick":
                            if (backend.isKick(output, name, command.split(" ")[1])) {
                                backend.kickMess(command.split(" ")[1]);
                                //backend.clientOffline(command.split(" ")[1]);
                                //clientSockets.get(command.split(" ")[1]).close();
                                //clientSockets.remove(name);
                            }
                            break;
                        case "/registration":
                            name = backend.reg(input, output, command);
                            clientSockets.put(name, sock);
                            break;
                        case "/grant":
                            backend.grant(output, command, name);
                            break;
                        case "/revoke":
                            backend.revoke(output, command, name);
                            break;
                        case "/chat":
                            backend.chat(input, output, command, name);
                            break;
                        case "/list":
                            backend.listClients(output, name);
                            break;
                        case "/stats":
                            backend.getStats(output, command, name);
                            break;
                        case "/unread":
                            backend.getUnread(output, name);
                            break;
                        case "/history":
                            backend.getHistory(output, command, name);
                            break;
                        default:
                            if (command.split(" ")[0].startsWith("/")) {
                                output.writeUTF("Server: incorrect command!");
                                output.flush();
                            } else {
                                backend.unknown(input, output, command, name);
                            }
                            break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        System.out.println("Server started!");
        backend = new ServerBack();
        new Server().go();
    }

    public boolean checkThreads() {
        Thread t = Thread.currentThread();
        String name = t.getName();
        System.out.println("name = " + name);
        return true;
    }

    public void go() {
        ServerSocket srvsocket = null;
        try {
            try {
                int i = 1;
                srvsocket = new ServerSocket(23456, 0, InetAddress.getByName("127.0.0.1"));
                srvsocket.setSoTimeout(7000);

                while (true) {
                    Socket clientSocket = srvsocket.accept();
                    Thread t = new Thread(new ClientHandler(clientSocket, i));
                    t.start();
                    System.out.println("Client " + i + " connected!");
                    i++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (srvsocket != null)
                    srvsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
}

