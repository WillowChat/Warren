package engineer.carrot.warren.irc.handlers.RPL;

import engineer.carrot.warren.event.TopicNotifyEvent;
import engineer.carrot.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.irc.messages.RPL.TopicMessage;

import javax.annotation.Nonnull;

public class TopicHandler extends MessageHandler<TopicMessage> {
    @Override
    public void handleMessage(@Nonnull TopicMessage message) {
        this.postEvent(new TopicNotifyEvent(message.forChannel, message.contents));
    }
}
