package engineer.carrot.warren.warren.irc.handlers.core;

import engineer.carrot.warren.warren.ctcp.CtcpEnum;
import engineer.carrot.warren.warren.ctcp.CtcpHelper;
import engineer.carrot.warren.warren.event.ChannelActionEvent;
import engineer.carrot.warren.warren.event.ChannelMessageEvent;
import engineer.carrot.warren.warren.event.PrivateActionEvent;
import engineer.carrot.warren.warren.event.PrivateMessageEvent;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.PrivMsgMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class PrivMsgHandler extends MessageHandler<PrivMsgMessage> {
    final Logger LOGGER = LoggerFactory.getLogger(PrivMsgHandler.class);

    @Override
    public void handleMessage(@Nonnull PrivMsgMessage message) {
        String contents = message.contents;
        CtcpEnum ctcp = CtcpEnum.NONE;

        if (CtcpHelper.isMessageCTCP(message.contents)) {
            ctcp = CtcpEnum.parseFromMessage(contents);
            contents = CtcpHelper.trimCTCP(contents);

            if (ctcp == CtcpEnum.UNKNOWN) {
                LOGGER.warn("Dropping unknown CTCP message from {}: {}", message.fromUser, message.contents);

                return;
            }
        }

        if (message.toTarget.equalsIgnoreCase(this.botDelegate.getBotNickname())) {
            User fromUser = this.botDelegate.getUserManager().getOrCreateUser(message.fromUser);

            if (ctcp == CtcpEnum.NONE) {
                this.postEvent(new PrivateMessageEvent(fromUser, message.toTarget, contents));
            } else if (ctcp == CtcpEnum.ACTION) {
                this.postEvent(new PrivateActionEvent(fromUser, message.toTarget, contents));
            }
        } else {
            Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.toTarget);
            User fromUser = channel.getOrCreateUser(message.fromUser, this.botDelegate.getUserManager());

            if (channel == null) {
                LOGGER.warn("Got a message from a channel that the bot doesn't think it's in! {} {}", fromUser.getNameWithoutAccess(), contents);
            } else {
                if (ctcp == CtcpEnum.NONE) {
                    this.postEvent(new ChannelMessageEvent(fromUser, channel, contents));
                } else if (ctcp == CtcpEnum.ACTION) {
                    this.postEvent(new ChannelActionEvent(fromUser, channel, contents));
                }
            }
        }
    }
}
