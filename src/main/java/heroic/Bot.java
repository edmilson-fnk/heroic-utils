package heroic;

import heroic.commands.Count;
import heroic.commands.StopCount;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.listener.message.MessageCreateListener;

import static heroic.Constants.COUNT_COMMAND;
import static heroic.Constants.STOP_COMMAND;

public class Bot {

    private static CounterThread countThread = null;

    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");
        DiscordApiBuilder discordApiBuilder = new DiscordApiBuilder();
        discordApiBuilder.setWaitForServersOnStartup(true);
        DiscordApi api = discordApiBuilder.setToken(token).login().join();
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
            String[] tokens = msg.split("\n");
            String command = tokens[0].toLowerCase();
            TextChannel currentChannel = event.getChannel();

            if (command.startsWith("!" + COUNT_COMMAND)) {
                countThread = Count.run(tokens, currentChannel, api, countThread);
            } else if (command.startsWith("!" + STOP_COMMAND)) {
                StopCount.run(tokens, currentChannel, api, countThread);
                countThread = null;
            }
        };
    }

}
