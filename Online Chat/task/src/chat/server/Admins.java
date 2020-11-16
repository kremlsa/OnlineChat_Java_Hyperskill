package chat.server;

import java.io.DataOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Admins {
    List<String> moderators = Collections.synchronizedList(new ArrayList<String>());
    Map<String, Long> banned = Collections.synchronizedMap(new HashMap<String, Long>());

    public void addModerators(String moder) {
        moderators.add(moder);
    }

    public void remModerators(String moder) {
        moderators.remove(moder);
    }

    public void addBan(String name) {
        banned.put(name, System.nanoTime());
    }

    public boolean isBanned(String name) {
        if (!banned.containsKey(name)) return false;
        long convert = TimeUnit.MINUTES.convert(System.nanoTime() - banned.get(name), TimeUnit.NANOSECONDS);
        return convert < 5L;
    }

    public boolean isModerator(String name) {
        return moderators.contains(name);
    }

}
