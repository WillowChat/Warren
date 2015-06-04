package engineer.carrot.warren.warren.irc.handlers.RPL;

import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.RPL.WelcomeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomeHandler extends MessageHandler<WelcomeMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(WelcomeHandler.class);

    @Override
    public void handleMessage(WelcomeMessage message) {
        LOGGER.info("Got server welcome: {} -> {} '{}'", message.forServer, message.toTarget, message.contents);
    }
}
