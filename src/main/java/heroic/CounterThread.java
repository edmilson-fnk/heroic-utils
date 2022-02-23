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

    private Collection<ServerVoiceChannel> channels;
    private int minutesLive;
    private int timeToWatch;

    public CounterThread(Collection<ServerVoiceChannel> channels, int timeToWatch) {
        this.channels = channels;
        this.minutesLive = 0;
        this.timeToWatch = timeToWatch;
    }

    @Override
    public void run() {
        Map<Long, Map<ServerVoiceChannel, Collection<User>>> counts = new TreeMap<>();

        while (this.minutesLive < this.timeToWatch) {
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
