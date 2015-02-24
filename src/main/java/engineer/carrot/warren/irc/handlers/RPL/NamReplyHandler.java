package engineer.carrot.warren.irc.handlers.RPL;

import com.google.gson.Gson;
import engineer.carrot.warren.irc.messages.RPL.NamReplyMessage;
import engineer.carrot.warren.Channel;
import engineer.carrot.warren.irc.handlers.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class NamReplyHandler extends MessageHandler<NamReplyMessage> {
    final Logger LOGGER = LoggerFactory.getLogger(NamReplyHandler.class);

    @Override
    public void handleMessage(@Nonnull NamReplyMessage message) {
        // TODO: Add to buffer and process in ENDOFNAMES (366) instead (could be multiple NAMREPLYs)

        if (!message.forUser.equalsIgnoreCase(this.botDelegate.getBotNickname())) {
            LOGGER.warn("Nam reply for someone that isn't us! {} {}", message.forChannel, message.users);
            return;
        }

        Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.forChannel);
        if (channel == null) {
            LOGGER.warn("Nam reply for channel we're not in {} {}", message.forChannel, message.users);
            return;
        }

        channel.parseNewUserList(message.users);
        LOGGER.info("Set new user list for {}: {}", message.forChannel, new Gson().toJson(channel.users));
        LOGGER.info("Access levels: {}", channel.userAccessMap);
    }
}
