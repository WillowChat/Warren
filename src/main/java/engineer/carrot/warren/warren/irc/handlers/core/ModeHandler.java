package engineer.carrot.warren.warren.irc.handlers.core;

import com.google.gson.Gson;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.ModeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ModeHandler extends MessageHandler<ModeMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(ModeHandler.class);

    @Override
    public void handleMessage(ModeMessage message) {
        Set<String> channelPrefixes = this.incomingHandler.getISupportManager().getChannelPrefixesModule().getChannelPrefixes();
        String prefix = message.target.substring(0, 1);
        if (channelPrefixes.contains(prefix)) {
            // Channel MODE

            Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.target);
            if (channel != null) {
                LOGGER.info("Handling channel MODE for a channel we're in:");
            } else {
                LOGGER.info("Got a channel MODE for a channel we don't think we're in:");
            }

            LOGGER.info("{}", new Gson().toJson(message));

            return;
        }

        // User MODE

        LOGGER.info("Handling user MODE:");
        LOGGER.info("{}", new Gson().toJson(message));
    }
}