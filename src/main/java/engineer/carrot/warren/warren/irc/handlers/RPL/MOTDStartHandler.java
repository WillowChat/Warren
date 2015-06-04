package engineer.carrot.warren.warren.irc.handlers.RPL;

import com.google.common.collect.Sets;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.warren.irc.messages.RPL.MOTDStartMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MOTDStartHandler extends MessageHandler<MOTDStartMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(MOTDStartHandler.class);

    @Override
    public void handleMessage(MOTDStartMessage message) {
        LOGGER.info("Starting MOTD: {} says '{}'", message.forServer, message.contents);

        this.incomingHandler.setNextExpectedCommands(Sets.newHashSet(MessageCodes.RPL.MOTD, MessageCodes.RPL.ENDOFMOTD));
    }
}
