package heroic;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static heroic.Constants.DELAY;

public class CounterThread extends Thread {

    private final TextChannel currentChannel;
    private final Collection<ServerVoiceChannel> channels;
    private final int timeToWatch;
    private final DiscordApi api;
    private final Server server;
    private int minutesAlive;
    private boolean shouldFinish;
    Map<Long, Map<ServerVoiceChannel, Collection<User>>> counts;

    public CounterThread(DiscordApi api, TextChannel currentChannel, Server server, Collection<ServerVoiceChannel> channels, int timeToWatch) {
        this.api = api;
        this.currentChannel = currentChannel;
        this.server = server;
        this.channels = channels;
        this.minutesAlive = 0;
        this.timeToWatch = timeToWatch;
        this.counts = null;
        this.shouldFinish = false;
    }

    @Override
    public void run() {
        this.counts = new TreeMap<>();

        while (true) {
            Map<ServerVoiceChannel, Collection<User>> usersByChannel = new HashMap<>();
            for (ServerVoiceChannel svc : this.channels) {
//                Collection<User> users = getSVCUsers(svc);
                Collection<Long> userIds = getSVCUserIds(svc);
                Collection<User> users = getUsersFromIds(new LinkedList<>(userIds));
                usersByChannel.put(svc, users);
            }
            counts.put(System.currentTimeMillis(), usersByChannel);

            this.minutesAlive += DELAY;
            if ((this.minutesAlive >= this.timeToWatch) || this.shouldFinish) {
                break;
            }

            waitSomeTime(DELAY);
        }

        try {
            String fileName = String.format("WoeWoc-%s", Utils.convertMsToDate(System.currentTimeMillis()));
            String absolutePath = new ExcelFile(fileName).generateWorkbook(this.server, this.counts);
            EmailSender.sendMail(absolutePath);
            currentChannel.sendMessage("E-mail enviado");
        } catch (IOException e) {
            currentChannel.sendMessage("Erro ao enviar e-mail");
            e.printStackTrace();
        }
    }

    private Collection<User> getUsersFromIds(Collection<Long> userIds) {
        Collection<User> users = new ArrayList<>();
        Iterator<Long> it = userIds.iterator();
        while (it.hasNext()) {
            Long userId = it.next();
            try {
                users.add(this.api.getUserById(userId).get());
            } catch (InterruptedException | ExecutionException e) {
                this.currentChannel.sendMessage("Erro no \"" + userId + "\": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return users;
    }

    public void finish() {
        currentChannel.sendMessage("Encerrando contagem, enviando e-mail em instantes");
        this.shouldFinish = true;
    }

    public Collection<User> getSVCUsers(ServerVoiceChannel svc) {
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
                e.printStackTrace();
            }
        }
    }

    public Collection<Long> getSVCUserIds(ServerVoiceChannel svc) {
        int retries = 0;
        while (true) {
            try {
                return svc.getConnectedUserIds();
            } catch (Exception e) {
                retries++;
                if (retries >= 5) {
                    return Collections.emptyList();
                }
                waitSomeTime(0.1F);
                e.printStackTrace();
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
