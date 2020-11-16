package chat.server;

import java.io.*;
import java.util.*;

public class ServerBack {
    Map<String, DataOutputStream> clientOut = new HashMap<String, DataOutputStream>();
    Map<String, String> userDB = new HashMap<String, String>();
    String userPath = "C:\\Java_lessons\\Online Chat\\Online Chat\\task\\src\\chat\\server\\db\\users.txt";
    String messPath = "C:\\Java_lessons\\Online Chat\\Online Chat\\task\\src\\chat\\server\\db\\mess.txt";
    File userFile = new File(userPath);
    File messFile = new File(messPath);
    List<String> activeClients = Collections.synchronizedList(new ArrayList<String>());
    static List<String[]> messages = Collections.synchronizedList(new ArrayList<String[]>());
    Map<String, String> chatPairs = Collections.synchronizedMap(new HashMap<String, String>());

    ServerBack() {
        loadUser();
        loadMess();
    }

    public void clientOnline(String name) {
        activeClients.add(name);
    }

    public void clientOffline(String name) {
        clientOut.remove(name);
        activeClients.remove(name);
        chatPairs.remove(name);
    }

    public void listClients(DataOutputStream output, String name) {
        String res = "Server: online:";
        for (String s : activeClients) {
            if (!s.equals(name)) res += " " + s;
        }
        if (!res.equals("Server: online:")) {
            try {
                output.writeUTF(res);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                output.writeUTF("Server: no one online");
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void chat(DataInputStream input, DataOutputStream output, String command, String name) {
        if (activeClients.contains(name)) {
            try {
                    if (activeClients.contains(command.split(" ")[1])) {
                        chatPairs.put(name,command.split(" ")[1] );
                        viewMessages(command.split(" ")[1],name, output);
                    } else {
                        output.writeUTF("Server: the user is not online!");
                        output.flush();
                    }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                output.writeUTF("Server: you are not in the chat!");
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void unknown(DataInputStream input, DataOutputStream output, String command, String name) {
        if (activeClients.contains(name) && chatPairs.containsKey(name)) {
            try {
                String recepient = chatPairs.get(name);
                if (chatPairs.containsKey(name) && chatPairs.containsKey(recepient)) {
                    if (chatPairs.get(name).equals(recepient) && chatPairs.get(recepient).equals(name)) {
                        DataOutputStream outputClient= clientOut.get(recepient);
                        messages.add(new String[]{recepient, name, name + ": " + command, "read"});
                        saveMess();
                        outputClient.writeUTF(name + ": " + command);
                        outputClient.flush();
                        DataOutputStream outputSender= clientOut.get(name);
                        outputSender.writeUTF(name + ": " + command);
                        outputSender.flush();
                    }
                } else {
                    try {
                        DataOutputStream outputSender= clientOut.get(name);
                        outputSender.writeUTF(name + ": " + command);
                        outputSender.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    messages.add(new String[]{recepient, name, name + ": " + command, "new"});
                    saveMess();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (activeClients.contains(name)) {
            try {
                output.writeUTF("Server: use /list command to choose a user to text!");
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
                try {
                    output.writeUTF("Server: you are not in the chat!");
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    public String auth(DataInputStream input, DataOutputStream output, String command) {
        String name = "";
        try {
            if (command.split(" ").length == 3) {
                name = command.split(" ")[1];
                String hash = String.valueOf(command.split(" ")[2].hashCode());
                if (!userDB.containsKey(name)) {
                    output.writeUTF("Server: incorrect login!");
                    output.flush();
                } else if (!userDB.get(name).equals(hash)) {
                    output.writeUTF("Server: incorrect password!");
                    output.flush();
                } else {
                    clientOut.put(name, output);
                    output.writeUTF("Server: you are authorized successfully!");
                    clientOnline(name);
                    output.flush();
                }
            } else {
                output.writeUTF("Server: incorrect command!");
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    public String reg(DataInputStream input, DataOutputStream output, String command) {
        String name = "";
        try {
            if (command.split(" ").length == 3) {
                name = command.split(" ")[1];
                if (userDB.containsKey(command.split(" ")[1])) {
                    output.writeUTF("Server: this login is already taken! Choose another one.");
                    output.flush();
                } else if (command.split(" ")[2].length() < 8) {
                    output.writeUTF("Server: the password is too short!");
                    output.flush();
                } else {
                    clientOut.put(name, output);
                    output.writeUTF("Server: you are registered successfully!");
                    output.flush();
                    clientOnline(name);
                    userDB.put(name, String.valueOf(command.split(" ")[2].hashCode()));
                    saveUser();
                }
            } else {
                output.writeUTF("Server: incorrect command!");
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    public void viewMessages(String sender, String recepient, DataOutputStream output) {
        if (messages.size() > 0) {

            List<String> topten = new ArrayList<>();

            for (int j = 0; j < messages.size(); j++) {
                String to = messages.get(j)[0];
                String from = messages.get(j)[1];
                String mes = messages.get(j)[2];
                String status = messages.get(j)[3].equals("new") ? "(new) " : "";
                if (to.equals(recepient) && from.equals(sender) || to.equals(sender) && from.equals(recepient)) {
                    topten.add(status + mes);
                    if (status.equals("(new) ")) {
                        messages.set(j, new String[]{to, from, mes, "old"});
                    }
                }
            }

            int startPos;
            if (topten.size() > 10) {
                startPos = topten.size() - 10;
            } else {
                startPos = 0;
            }

            for (int k = startPos; k < topten.size(); k++) {
                try {
                    output.writeUTF(topten.get(k));
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            saveMess();
        }
    }

    void loadUser () {
        BufferedReader reader = null;
        if (userFile.exists()) {
            try {
                reader = new BufferedReader(new FileReader(userFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String line = null;
            while (true) {
                try {
                    if (!((line = reader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (line.contains(";")) {
                    String[] strings = line.split(";");
                    userDB.put(strings[0], strings[1]);
                }
            }
        }
    }

        void saveUser() {
            String res = "";
            for (Map.Entry<String, String> entry : userDB.entrySet()) {
                res = res + entry.getKey() + ";" + entry.getValue() + "\n";
            }
            BufferedWriter bf = null;
            try {
                bf = new BufferedWriter(new FileWriter(userFile));
                bf.write(res);
                bf.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    bf.close();
                } catch (Exception e) {
                }
            }

        }

    void loadMess () {
        BufferedReader reader = null;
        if (messFile.exists()) {
            try {
                reader = new BufferedReader(new FileReader(messFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String line = null;
            while (true) {
                try {
                    if (!((line = reader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (line.contains(";")) {
                    String[] strings = line.split(";");
                    messages.add(new String[]{strings[0], strings[1], strings[2], strings[3]});
                }
            }
        }
    }

    void saveMess() {
        String res = "";
        for (String[] m : messages) {
            res = res + m[0] + ";" + m[1] + ";" + m[2] + ";" + m[3] + "\n";
        }
        BufferedWriter bf = null;
        try {
            bf = new BufferedWriter(new FileWriter(messFile));
            bf.write(res);
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                bf.close();
            } catch (Exception e) {
            }
        }

    }
}
