package engineer.carrot.warren.irc.handlers.core;

import engineer.carrot.warren.event.ChannelMessageEvent;
import engineer.carrot.warren.event.PrivateMessageEvent;
import engineer.carrot.warren.irc.Channel;
import engineer.carrot.warren.irc.User;
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
            User fromUser = this.botDelegate.getUserManager().getOrCreateUser(message.fromUser);

            this.postEvent(new PrivateMessageEvent(fromUser, message.toTarget, message.contents));
        } else {
            Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.toTarget);
            User fromUser = channel.getOrCreateUser(message.fromUser, this.botDelegate.getUserManager());

            if (channel == null) {
                LOGGER.warn("Got a message from a channel that the bot doesn't think it's in! {} {}", fromUser.getName(), message.contents);
            } else {
                this.postEvent(new ChannelMessageEvent(fromUser, channel, message.contents));
            }
        }
    }
}
