package heroic;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CounterThread extends Thread {

    private final TextChannel currentChannel;
    private final Collection<ServerVoiceChannel> channels;
    private final int timeToWatch;
    private final DiscordApi api;
    private final Server server;
    private boolean shouldFinish;
    Map<Long, Map<ServerVoiceChannel, Collection<User>>> counts;
    Map<Long, Map<Long, Boolean>> deafStatus;

    public CounterThread(DiscordApi api, TextChannel currentChannel, Server server, Collection<ServerVoiceChannel> channels, int timeToWatch) {
        this.api = api;
        this.currentChannel = currentChannel;
        this.server = server;
        this.channels = channels;
        this.timeToWatch = timeToWatch;
        this.counts = null;
        this.deafStatus = null;
        this.shouldFinish = false;
    }

    @Override
    public void run() {
        this.counts = new TreeMap<>();
        this.deafStatus = new TreeMap<>();

        long startTime = System.currentTimeMillis();
        int minutesAlive = 0;
        while (true) {
            while (new DateTime().getMinuteOfHour() % 10 != 0) {
                waitSomeTime(1);
                minutesAlive = (int) ((System.currentTimeMillis() - startTime) / (1000 * 60));
                if (this.shouldFinish || minutesAlive >= this.timeToWatch) {
                    break;
                }
            }

            Map<ServerVoiceChannel, Collection<User>> usersByChannel = new HashMap<>();
            long currentTimeKey = System.currentTimeMillis();
            this.counts.put(currentTimeKey, usersByChannel);
            this.deafStatus.put(currentTimeKey, new HashMap<>());
            for (ServerVoiceChannel svc : this.channels) {
                Collection<Long> userIds = getSVCUserIds(svc);
                Map<Long, Boolean> deafStatus = userIds.stream().collect(
                        Collectors.toMap(u -> u, u -> svc.getServer().isSelfDeafened(u))
                );
                this.deafStatus.get(currentTimeKey).putAll(deafStatus);
                Collection<User> users = getUsersFromIds(new LinkedList<>(userIds));
                usersByChannel.put(svc, users);
            }

            waitSomeTime(1);
            if ((minutesAlive >= this.timeToWatch) || this.shouldFinish) {
                break;
            }
        }

        try {
            String fileName = String.format("WoeWoc-%s", Utils.convertMsToDate(System.currentTimeMillis()));
            String absolutePath = new ExcelFile(fileName).generateWorkbook(this.server, this.counts, this.deafStatus);
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
