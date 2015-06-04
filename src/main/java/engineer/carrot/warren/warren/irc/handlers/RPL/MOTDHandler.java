package engineer.carrot.warren.warren.irc.handlers.RPL;

import com.google.common.collect.Sets;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.warren.irc.messages.RPL.MOTDMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MOTDHandler extends MessageHandler<MOTDMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(MOTDHandler.class);

    @Override
    public void handleMessage(MOTDMessage message) {
        LOGGER.info("MOTD: {} adds '{}'", message.forServer, message.contents);

        this.incomingHandler.setNextExpectedCommands(Sets.newHashSet(MessageCodes.RPL.MOTD, MessageCodes.RPL.ENDOFMOTD));
    }
}
