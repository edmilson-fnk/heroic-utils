package heroic;

import heroic.commands.Count;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.listener.message.MessageCreateListener;

import static heroic.Constants.COUNT_COMMAND;

public class Bot {

    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");
        DiscordApiBuilder discordApiBuilder = new DiscordApiBuilder();
        discordApiBuilder.setWaitForServersOnStartup(true);
        DiscordApi api = discordApiBuilder.setToken(token).login().join();
        api.setMessageCacheSize(0, 0);
        api.addMessageCreateListener(getVoiceChannelListener(api));
        System.out.println("Heroic Bot iniciado");
    }

    private static MessageCreateListener getVoiceChannelListener(DiscordApi api) {
        return event -> {
            MessageAuthor messageAuthor = event.getMessageAuthor();
            if (messageAuthor.isBotUser()) {
                return;
            }

            String msg = event.getMessageContent();
            String[] tokens = msg.split(" ");
            String command = tokens[0].toLowerCase();
            TextChannel channel = event.getChannel();

            if (command.startsWith("!" + COUNT_COMMAND)) {
                Count.run(tokens, channel, api);
            }
        };
    }

}
