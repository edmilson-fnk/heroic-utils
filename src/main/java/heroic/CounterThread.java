package heroic;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CounterThread extends Thread {

    private final int MINUTES_TO_WATCH = 3; // minutes to watch the channel for
    private final int DELAY = 1; // minutes to wait before recounting

    private Collection<ServerVoiceChannel> channels;
    private int minutesLive;

    public CounterThread(Collection<ServerVoiceChannel> channels) {
        this.channels = channels;
        this.minutesLive = 0;
    }

    @Override
    public void run() {
        Map<Long, Map<ServerVoiceChannel, Collection<User>>> counts = new TreeMap<>();

        while (this.minutesLive < MINUTES_TO_WATCH) {
            this.minutesLive += DELAY;

            Map<ServerVoiceChannel, Collection<User>> usersByChannel = new HashMap<>();
            for (ServerVoiceChannel svc : this.channels) {
                Collection<User> users = getUsers(svc);
                usersByChannel.put(svc, users);
            }
            counts.put(System.currentTimeMillis(), usersByChannel);

            waitSomeMinutes();
        }

        try {
            String fileName = String.format("WoeWoc-%s", Utils.convertMsToDate(System.currentTimeMillis()));
            new ExcelFile(fileName).generateWorkbook(counts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Collection<User> getUsers(ServerVoiceChannel svc) {
        return svc.getConnectedUsers();
    }

    private void waitSomeMinutes() {
        try {
            Thread.sleep((long) DELAY * 1000 * 60);
        } catch (InterruptedException e) {
            // We've been interrupted
        }
    }

}
