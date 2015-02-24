package engineer.carrot.warren.irc.handlers.core;

import engineer.carrot.warren.Channel;
import engineer.carrot.warren.event.ChannelMessageEvent;
import engineer.carrot.warren.event.PrivateMessageEvent;
import engineer.carrot.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.irc.messages.core.PrivMsgMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class PrivMsgHandler extends MessageHandler<PrivMsgMessage> {
    final Logger LOGGER = LoggerFactory.getLogger(PrivMsgHandler.class);

    @Override
    public void handleMessage(@Nonnull PrivMsgMessage message) {
        if (message.toTarget.equalsIgnoreCase(this.botDelegate.getBotNickname())) {
            this.postEvent(new PrivateMessageEvent(message.fromUser, message.toTarget, message.contents));
        } else {
            Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.toTarget);
            if (channel == null) {
                LOGGER.warn("Got a message from a channel that the bot doesn't think it's in! {} {}", message.fromUser.user, message.contents);
            } else {
                this.postEvent(new ChannelMessageEvent(message.fromUser, channel.name, message.contents));
            }
        }
    }
}
