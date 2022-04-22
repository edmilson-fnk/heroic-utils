package heroic.commands;

import heroic.CounterThread;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import static heroic.Constants.MINUTES_TO_WATCH;
import static heroic.Constants.STOP_COMMAND;

public class Count {

    public static CounterThread run(String[] tokens, TextChannel currentChannel, Server server, DiscordApi api, CounterThread botCountThread) {
        if (tokens.length <= 1) {
            String helpMessage = getHelpMessage();
            currentChannel.sendMessage(helpMessage);
            return null;
        }

        if (botCountThread != null) {
            currentChannel.sendMessage(
                    String.format("Contagem já está em curso, use **!%s** antes de iniciar nova contagem.", STOP_COMMAND)
            );
            return botCountThread;
        }

        String[] channelsNames = Arrays.copyOfRange(tokens, 1, tokens.length);
        Collection<ServerVoiceChannel> channels = new LinkedList<>();
        for (String channelName : channelsNames) {
            channels.addAll(getChannels(channelName, api));
        }

        if (channels.isEmpty()) {
            currentChannel.sendMessage("Nenhum canal de voz encontrado");
        } else {
            String[] firstLine = tokens[0].split(" ");
            int timeToWatch = firstLine.length > 1 ? Integer.parseInt(firstLine[1]) : MINUTES_TO_WATCH;

            String names = channels.stream().map(Nameable::getName).collect(Collectors.joining(", "));
            String ids = channels.stream().map(DiscordEntity::getId).map(id -> Long.toString(id)).collect(Collectors.joining(", "));
            String msg = "Contando usuários nos canais: " + names;
            currentChannel.sendMessage(msg);
            System.out.println(msg);
            System.out.println("IDs dos canais: " + ids);
            CounterThread countThread = new CounterThread(api, currentChannel, server, channels, timeToWatch);
            countThread.start();
            return countThread;
        }
        return null;
    }

    public static Collection<ServerVoiceChannel> getChannels(String channelName, DiscordApi api) {
        Collection<Channel> channelsByName = api.getChannelsByName(channelName);
        Collection<ServerVoiceChannel> svcList = new LinkedList<>();
        for (Channel channel : channelsByName) {
            Optional<ServerVoiceChannel> svc = channel.asServerVoiceChannel();
            svc.ifPresent(svcList::add);
        }
        return svcList;
    }

    public static String getHelpMessage() {
        return "**Exemplos de uso do comando**:" +
                "\n```!contar\nCanal 1\nCanal 2```" +
                "Monitora os canais **Áudio 1** e **Áudio 2** por **100** minutos (tempo padrão)" +
                "\n```!contar 30\nCanal 1\nCanal 2```" +
                "Monitora os canais **Áudio 1** e **Áudio 2** por **30** minutos";
    }

}
