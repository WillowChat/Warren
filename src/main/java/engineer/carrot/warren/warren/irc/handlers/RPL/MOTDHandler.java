package engineer.carrot.warren.warren.irc.handlers.RPL;

import com.google.common.collect.Sets;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.handlers.multi.IMotdMultiHandler;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.warren.irc.messages.RPL.MOTDMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotdHandler extends MessageHandler<MOTDMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(MotdHandler.class);

    @Override
    public void handleMessage(MOTDMessage message) {
        IMotdMultiHandler handler = this.incomingHandler.getMotdHandler();
        if (!handler.isConstructing()) {
            LOGGER.warn("MOTD handler doesn't think it's constructing an MOTD yet - ignoring MOTD message");

            return;
        }

        handler.addMotdMessage(message.contents);

        this.incomingHandler.setNextExpectedCommands(Sets.newHashSet(MessageCodes.RPL.MOTD, MessageCodes.RPL.ENDOFMOTD));
    }
}
