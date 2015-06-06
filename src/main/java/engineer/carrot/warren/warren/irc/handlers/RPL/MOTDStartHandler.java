package engineer.carrot.warren.warren.irc.handlers.RPL;

import com.google.common.collect.Sets;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.handlers.multi.IMotdMultiHandler;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.warren.irc.messages.RPL.MOTDStartMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MotdStartHandler extends MessageHandler<MOTDStartMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(MotdStartHandler.class);

    @Override
    public void handleMessage(MOTDStartMessage message) {
        IMotdMultiHandler handler = this.incomingHandler.getMotdHandler();
        if (handler.isConstructing()) {
            LOGGER.warn("MOTD handler already thinks it's constructing an MOTD - clearing and starting again");
        }

        handler.startConstructing();
        handler.setForServer(message.forServer);
        handler.addMotdMessage(message.contents);

        this.incomingHandler.setNextExpectedCommands(Sets.newHashSet(MessageCodes.RPL.MOTD, MessageCodes.RPL.ENDOFMOTD));
    }
}
