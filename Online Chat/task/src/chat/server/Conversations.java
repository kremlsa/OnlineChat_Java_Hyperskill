package chat.server;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Conversations {
    static String messPath = "C:\\Java_lessons\\Online Chat\\Online Chat\\task\\src\\chat\\server\\db\\mess.txt";
    static File messFile = new File(messPath);
    static List<String[]> messages = Collections.synchronizedList(new ArrayList<String[]>());


    Conversations() {
        loadMess();
    }

    public static void viewMessages(String sender, String recepient, DataOutputStream output) {
        if (messages.size() > 0) {

            List<String> topten = new ArrayList<>();
            List<String> topNew = new ArrayList<>();

            for (int j = 0; j < messages.size(); j++) {
                String to = messages.get(j)[0];
                String from = messages.get(j)[1];
                String mes = messages.get(j)[2];
                String status = messages.get(j)[3].equals("new") ? "(new) " : "";
                if (to.equals(recepient) && from.equals(sender) || to.equals(sender) && from.equals(recepient)) {
                    if (status.equals("(new) ")) {
                        topNew.add(status + mes);
                        messages.set(j, new String[]{to, from, mes, "old"});
                    } else {
                        topten.add(status + mes);
                    }
                }
            }
            if (topNew.size() >= 25) {
                int startPos = topNew.size() - 25;
                for (int k = startPos; k < topNew.size(); k++) {
                    try {
                        output.writeUTF(topNew.get(k));
                        output.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                int delta = 25 - topNew.size();
                int startPos;
                int num = Math.min(delta, 10);
                if (topten.size() > num) {
                    startPos = topten.size() - num;
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

                for (int k = 0; k < topNew.size(); k++) {
                    try {
                        output.writeUTF(topNew.get(k));
                        output.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            saveMess();
        }
    }


    static void loadMess() {
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

    static void saveMess() {
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

    static void stats(String from, String to, DataOutputStream output) {
        int total = 0, countTo = 0, countFrom = 0;
        for (int j = 0; j < messages.size(); j++) {
            String messFrom = messages.get(j)[0];
            String messTo = messages.get(j)[1];
            if (to.equals(messTo) && from.equals(messFrom)) {
                countTo++;
            }

            if (to.equals(messFrom) && from.equals(messTo)) {
                countFrom++;
            }
        }
        total = countFrom + countTo;
        try {
            output.writeUTF("Server:");
            output.flush();
            output.writeUTF("Statistics with " + to + ":");
            output.flush();
            output.writeUTF("Total messages: " + total);
            output.flush();
            output.writeUTF("Messages from " + from + ": " + countFrom);
            output.flush();
            output.writeUTF("Messages from " + to + ": " + countTo);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void history(String sender, String recepient, String command, DataOutputStream output) {
        String n = "";

        if (command.split(" ").length > 1) {
            n = command.split(" ")[1];
        } else
        {
            try {
                output.writeUTF("Server: " + n + " is not a number!");
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (!n.matches("\\d+")) {
            try {
                output.writeUTF("Server: " + n + " is not a number!");
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        int num = Integer.parseInt(n);
        if (messages.size() > 0) {
            List<String> topN = new ArrayList<>();

            for (int j = 0; j < messages.size(); j++) {
                String to = messages.get(j)[0];
                String from = messages.get(j)[1];
                String mes = messages.get(j)[2];
                String status = messages.get(j)[3].equals("new") ? "(new) " : "";
                if (to.equals(recepient) && from.equals(sender) || to.equals(sender) && from.equals(recepient)) {
                    topN.add(mes);
                }
            }

            int startPos;
            if (topN.size() > num) {
                startPos = topN.size() - num;
            } else {
                startPos = 0;
            }
            int count = 0;
            try {
                output.writeUTF("Server:");
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int k = startPos; k < topN.size(); k++) {
                try {
                    output.writeUTF(topN.get(k));
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                count++;
                if (count > 24) break;
            }

        }
    }

    public static void unread(String sender, DataOutputStream output) {
            List<String> unreaded = new ArrayList<>();
            for (int j = 0; j < messages.size(); j++) {
                String to = messages.get(j)[0];
                String from = messages.get(j)[1];
                if (messages.get(j)[3].equals("new") && !from.equals(sender) && to.equals(sender)) {
                    if(!unreaded.contains(from))
                        unreaded.add(from);
                }
            }
            Collections.sort(unreaded);
            if (unreaded.size() > 0) {
                String res = "Server: unread from:";
                for (String s : unreaded) {
                    res += " " + s;
                }
                try {
                    output.writeUTF(res);
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    output.writeUTF("Server: no one unread");
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
}