package engineer.carrot.warren.warren.irc.handlers.core;

import engineer.carrot.warren.warren.event.UserPartedChannelEvent;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.PartChannelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartHandler extends MessageHandler<PartChannelMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(PartHandler.class);

    @Override
    public void handleMessage(PartChannelMessage message) {
        Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.channel);
        if (channel == null) {
            LOGGER.warn("We were notified of a user parting a channel that we aren't in! {} -> {}", message.channel, message.user);
            return;
        }

        User user = channel.getOrCreateUser(message.user, this.botDelegate.getUserManager());
        channel.removeUser(user);

        LOGGER.info("<{}> left {}: '{}'", user.getNameWithoutAccess(), channel.name, message.message);
        this.postEvent(new UserPartedChannelEvent(user, channel, message.message));
    }
}
