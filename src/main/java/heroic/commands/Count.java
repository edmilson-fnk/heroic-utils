package heroic.commands;

import heroic.CounterThread;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

public class Count {

    public static void run(String[] tokens, TextChannel channel, DiscordApi api) {
        if (tokens.length <= 1) {
            return;
        }

        String channelName = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));

        Collection<ServerVoiceChannel> channels = getChannels(channelName, api);
        if (channels.isEmpty()) {
            channel.sendMessage("Canal de voz **" + channelName +  "** não encontrado");
        } else {
            channel.sendMessage("Contando usuários em **" + channelName +  "**");
            new CounterThread(channels).start();
        }
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

}
