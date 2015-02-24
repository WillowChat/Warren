package engineer.carrot.warren.irc.handlers.RPL;

import engineer.carrot.warren.irc.messages.RPL.NoTopicMessage;
import engineer.carrot.warren.event.TopicNotifyEvent;
import engineer.carrot.warren.irc.handlers.MessageHandler;

import javax.annotation.Nonnull;

public class NoTopicHandler extends MessageHandler<NoTopicMessage> {
    @Override
    public void handleMessage(@Nonnull NoTopicMessage message) {
        this.postEvent(new TopicNotifyEvent(message.forChannel, ""));
    }
}
