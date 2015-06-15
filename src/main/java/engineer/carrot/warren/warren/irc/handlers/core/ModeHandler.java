package engineer.carrot.warren.warren.irc.handlers.core;

import com.google.gson.Gson;
import engineer.carrot.warren.warren.irc.AccessLevel;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.User;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.ModeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class ModeHandler extends MessageHandler<ModeMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(ModeHandler.class);

    @Override
    public void handleMessage(ModeMessage message) {
        Set<String> channelPrefixes = this.incomingHandler.getISupportManager().getChannelPrefixesModule().getChannelPrefixes();
        String prefix = message.target.substring(0, 1);
        if (channelPrefixes.contains(prefix)) {
            // Channel MODE

            Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.target);
            if (channel == null) {
                LOGGER.info("Got a channel MODE for a channel we don't think we're in:");
                LOGGER.info("{}", new Gson().toJson(message));

                return;
            }

            LOGGER.info("Handling channel MODE for a channel we're in:");
            LOGGER.info("{}", new Gson().toJson(message));

            List<ModeMessage.ModeModifier> modifiers = message.modifiers;
            for (ModeMessage.ModeModifier modifier : modifiers) {
                if (modifier.mode.equals("o")) {
                    if (!modifier.hasParameter()) {
                        LOGGER.warn("User got OP but a parameter (who) wasn't set");

                        continue;
                    }

                    User user = channel.getOrCreateUser(Hostmask.parseFromString(modifier.parameter), this.botDelegate.getUserManager());

                    if (modifier.isAdding()) {
                        channel.setUserAccessLevel(user, AccessLevel.OP);

                        LOGGER.info("User '{}' got OP in channel '{}'", user.getNameWithoutAccess(), channel.name);
                    } else {
                        channel.setUserAccessLevel(user, AccessLevel.NONE);

                        LOGGER.info("User '{}' got DEOPPED in channel '{}'", user.getNameWithoutAccess(), channel.name);
                    }
                }
            }

            return;
        }

        // User MODE

        LOGGER.info("Handling user MODE:");
        LOGGER.info("{}", new Gson().toJson(message));
    }
}