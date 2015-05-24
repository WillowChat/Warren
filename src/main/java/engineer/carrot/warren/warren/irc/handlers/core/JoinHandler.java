package engineer.carrot.warren.warren.irc.handlers.core;

import engineer.carrot.warren.warren.event.ClientJoinedChannelEvent;
import engineer.carrot.warren.warren.event.UserJoinedChannelEvent;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.JoinedChannelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class JoinHandler extends MessageHandler<JoinedChannelMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(JoinHandler.class);

    @Override
    public void handleMessage(@Nonnull JoinedChannelMessage message) {
        // TODO: Is this valid if the bot changes nicknames directly after joining a channel?
        if (message.user.user.equalsIgnoreCase(this.botDelegate.getBotNickname())) {
            this.botDelegate.moveJoiningChannelToJoined(message.channel);

            this.postEvent(new ClientJoinedChannelEvent(this.botDelegate.getJoinedChannels().getChannel(message.channel)));
        } else {
            Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.channel);
            if (channel == null) {
                LOGGER.warn("We were notified of a user joining a channel that we aren't in! {} -> {}", message.channel, message.user);
                return;
            }

            User user = channel.getOrCreateUser(message.user, this.botDelegate.getUserManager());
            LOGGER.info("<{}> joined {}", user.getNameWithoutAccess(), channel.name);
            this.postEvent(new UserJoinedChannelEvent(user, channel));
        }
    }
}
