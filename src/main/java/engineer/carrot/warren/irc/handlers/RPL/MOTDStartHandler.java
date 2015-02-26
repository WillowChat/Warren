package engineer.carrot.warren.irc.handlers.RPL;

import com.google.common.collect.Sets;
import engineer.carrot.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.irc.messages.RPL.MOTDStartMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class MOTDStartHandler extends MessageHandler<MOTDStartMessage> {
    final Logger LOGGER = LoggerFactory.getLogger(MOTDStartHandler.class);

    @Override
    public void handleMessage(@Nonnull MOTDStartMessage message) {
        LOGGER.info("Starting MOTD: {} says '{}'", message.forServer, message.contents);

        this.incomingHandler.setNextExpectedCommands(Sets.newHashSet(MessageCodes.RPL.MOTD, MessageCodes.RPL.ENDOFMOTD));
    }
}
