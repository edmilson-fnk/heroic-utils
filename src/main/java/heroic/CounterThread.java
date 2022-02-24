package heroic;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static heroic.Constants.DELAY;

public class CounterThread extends Thread {

    private final Collection<ServerVoiceChannel> channels;
    private final int timeToWatch;
    private int minutesAlive;

    public CounterThread(Collection<ServerVoiceChannel> channels, int timeToWatch) {
        this.channels = channels;
        this.minutesAlive = 0;
        this.timeToWatch = timeToWatch;
    }

    @Override
    public void run() {
        Map<Long, Map<ServerVoiceChannel, Collection<User>>> counts = new TreeMap<>();

        while (true) {
            Map<ServerVoiceChannel, Collection<User>> usersByChannel = new HashMap<>();
            for (ServerVoiceChannel svc : this.channels) {
                Collection<User> users = getUsers(svc);
                usersByChannel.put(svc, users);
            }
            counts.put(System.currentTimeMillis(), usersByChannel);

            this.minutesAlive += DELAY;
            if (this.minutesAlive >= this.timeToWatch) {
                break;
            }

            waitSomeMinutes();
        }

        try {
            String fileName = String.format("WoeWoc-%s", Utils.convertMsToDate(System.currentTimeMillis()));
            String absolutePath = new ExcelFile(fileName).generateWorkbook(counts);
            EmailSender.sendMail(absolutePath);
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
