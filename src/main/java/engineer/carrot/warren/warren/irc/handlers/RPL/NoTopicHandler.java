package engineer.carrot.warren.warren.irc.handlers.RPL;

import engineer.carrot.warren.warren.event.TopicNotifyEvent;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.RPL.NoTopicMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class NoTopicHandler extends MessageHandler<NoTopicMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(NoTopicHandler.class);

    @Override
    public void handleMessage(@Nonnull NoTopicMessage message) {
        Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.forChannel);
        if (channel == null) {
            LOGGER.warn("Got a no topic message for a channel we aren't in: {}", message.forChannel);

            return;
        }

        this.postEvent(new TopicNotifyEvent(channel, ""));
    }
}
