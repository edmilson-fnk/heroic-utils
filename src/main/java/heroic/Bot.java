package heroic;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.Collection;
import java.util.Optional;

public class Bot {

    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");
        DiscordApiBuilder discordApiBuilder = new DiscordApiBuilder();
        discordApiBuilder.setWaitForServersOnStartup(true);
        DiscordApi api = discordApiBuilder.setToken(token).login().join();
        api.setMessageCacheSize(0, 0);
        api.addMessageCreateListener(getVoiceChannelListener(api));
    }

    private static MessageCreateListener getVoiceChannelListener(DiscordApi api) {
        return event -> {
            MessageAuthor messageAuthor = event.getMessageAuthor();
            if (messageAuthor.isBotUser()) {
                return;
            }

            String msg = event.getMessageContent().toLowerCase();
            Collection<Channel> channels = api.getChannelsByName("General");
            for (Channel channel : channels) {
                Optional<ServerVoiceChannel> svc = channel.asServerVoiceChannel();
                if (svc.isPresent()) {
                    Collection<Long> users = svc.get().getConnectedUserIds();
                    System.out.println(users);
                }
            }

//            event.getChannel().asServerVoiceChannel().get().getConnectedUserIds();

        };
    }

}
