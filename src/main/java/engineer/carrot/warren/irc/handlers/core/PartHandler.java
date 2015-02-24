package engineer.carrot.warren.irc.handlers.core;

import engineer.carrot.warren.event.UserPartedChannelEvent;
import engineer.carrot.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.irc.messages.core.PartChannelMessage;

import javax.annotation.Nonnull;

public class PartHandler extends MessageHandler<PartChannelMessage> {
    @Override
    public void handleMessage(@Nonnull PartChannelMessage message) {
        this.postEvent(new UserPartedChannelEvent(message.user, message.channel, message.message));
    }
}
