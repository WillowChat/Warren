package engineer.carrot.warren.warren.irc.handlers.RPL;

import engineer.carrot.warren.warren.event.TopicNotifyEvent;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.RPL.TopicMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicHandler extends MessageHandler<TopicMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(TopicHandler.class);

    @Override
    public void handleMessage(TopicMessage message) {
        Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.forChannel);
        if (channel == null) {
            LOGGER.warn("Got a topic message for a channel we aren't in: {} '{}'", message.forChannel, message.contents);

            return;
        }

        this.eventSink.postEvent(new TopicNotifyEvent(channel, message.contents));
    }
}
