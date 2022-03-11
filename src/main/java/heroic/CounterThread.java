package heroic;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;

import java.io.IOException;
import java.util.*;

import static heroic.Constants.DELAY;

public class CounterThread extends Thread {

    private final TextChannel currentChannel;
    private final Collection<ServerVoiceChannel> channels;
    private final int timeToWatch;
    private int minutesAlive;
    private boolean shouldFinish;
    Map<Long, Map<ServerVoiceChannel, Collection<User>>> counts;

    public CounterThread(TextChannel currentChannel, Collection<ServerVoiceChannel> channels, int timeToWatch) {
        this.currentChannel = currentChannel;
        this.channels = channels;
        this.minutesAlive = 0;
        this.timeToWatch = timeToWatch;
        this.counts = null;
        this.shouldFinish = false;
    }

    @Override
    public void run() {
        this.counts = new TreeMap<>();

        while (!this.shouldFinish) {
            Map<ServerVoiceChannel, Collection<User>> usersByChannel = new HashMap<>();
            for (ServerVoiceChannel svc : this.channels) {
                Collection<User> users = getUsers(svc);
                usersByChannel.put(svc, users);
                System.out.printf("Users in %s: %d%n", svc.getName(), users.size());
            }
            counts.put(System.currentTimeMillis(), usersByChannel);

            System.out.printf("Thread alive: %d%n", this.minutesAlive);
            this.minutesAlive += DELAY;
            if (this.minutesAlive >= this.timeToWatch) {
                break;
            }

            waitSomeTime(DELAY);
        }

        try {
            String fileName = String.format("WoeWoc-%s", Utils.convertMsToDate(System.currentTimeMillis()));
            String absolutePath = new ExcelFile(fileName).generateWorkbook(this.counts);
            EmailSender.sendMail(absolutePath);
            currentChannel.sendMessage("E-mail enviado");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finish() {
        currentChannel.sendMessage("Encerrando contagem, enviando e-mail em instantes");
        this.shouldFinish = true;
    }

    public Collection<User> getUsers(ServerVoiceChannel svc) {
        int retries = 0;
        while (true) {
            try {
                return svc.getConnectedUsers();
            } catch (Exception e) {
                retries++;
                if (retries >= 5) {
                    return Collections.emptyList();
                }
                waitSomeTime(0.1F);
            }
        }
    }

    private void waitSomeTime(float delay) {
        try {
            for (int i = 0; i < delay && !this.shouldFinish; i++) {
                Thread.sleep(1000 * 60);
            }
        } catch (InterruptedException e) {
            // We've been interrupted
        }
    }

}
