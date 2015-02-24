package engineer.carrot.warren.irc.handlers.core;

import engineer.carrot.warren.event.ClientJoinedChannelEvent;
import engineer.carrot.warren.event.UserJoinedChannelEvent;
import engineer.carrot.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.irc.messages.core.JoinedChannelMessage;

import javax.annotation.Nonnull;

public class JoinHandler extends MessageHandler<JoinedChannelMessage> {
    @Override
    public void handleMessage(@Nonnull JoinedChannelMessage message) {
        // TODO: Is this valid if the bot changes nicknames directly after joining a channel?
        if (message.forUser.user.equalsIgnoreCase(this.botDelegate.getBotNickname())) {
            this.botDelegate.moveJoiningChannelToJoined(message.toTarget);

            this.postEvent(new ClientJoinedChannelEvent(message.toTarget));
        } else {
            this.postEvent(new UserJoinedChannelEvent(message.forUser, message.toTarget));
        }
    }
}
