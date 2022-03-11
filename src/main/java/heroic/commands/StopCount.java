package heroic.commands;

import heroic.CounterThread;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

public class StopCount {

    public static void run(String[] tokens, TextChannel currentChannel, DiscordApi api, CounterThread botCountThread) {
        if (botCountThread != null) {
            botCountThread.finish();
        } else {
            currentChannel.sendMessage("Não há contagem em curso");
        }
    }

}
