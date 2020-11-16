package chat.server;

import java.io.*;

public class Conversations {
    String messPath = "C:\\Java_lessons\\Online Chat\\Online Chat\\task\\src\\chat\\server\\db\\mess.txt";

    Conversations() {
        loadMess();
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
