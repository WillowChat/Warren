package engineer.carrot.warren.warren.irc.handlers.RPL;

import com.google.gson.Gson;
import engineer.carrot.warren.warren.UserManager;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.RPL.NamReplyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamReplyHandler extends MessageHandler<NamReplyMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(NamReplyHandler.class);

    @Override
    public void handleMessage(NamReplyMessage message) {
        // TODO: Add to buffer and process in ENDOFNAMES (366) instead (could be multiple NAMREPLYs)

        if (!message.forUser.equalsIgnoreCase(this.botDelegate.getBotNickname())) {
            LOGGER.warn("Nam reply for someone that isn't us! {} {}", message.forChannel, message.hostmasks);
            return;
        }

        Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.forChannel);
        if (channel == null) {
            LOGGER.warn("Nam reply for channel we're not in {} {}", message.forChannel, message.hostmasks);
            return;
        }

        UserManager userManager = this.botDelegate.getUserManager();

        for (Hostmask hostmask : message.hostmasks) {
            channel.getOrCreateUser(hostmask, userManager);
        }

        LOGGER.info("Set new user list for {}: {}", channel.name, new Gson().toJson(channel.users));
        LOGGER.info("Access levels: {}", channel.userModes);
    }
}
