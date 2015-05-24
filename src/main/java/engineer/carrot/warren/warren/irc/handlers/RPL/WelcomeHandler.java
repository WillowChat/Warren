package engineer.carrot.warren.warren.irc.handlers.RPL;

import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.RPL.WelcomeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class WelcomeHandler extends MessageHandler<WelcomeMessage> {
    final Logger LOGGER = LoggerFactory.getLogger(WelcomeHandler.class);

    @Override
    public void handleMessage(@Nonnull WelcomeMessage message) {
        LOGGER.info("Got server welcome: {} -> {} '{}'", message.forServer, message.toTarget, message.contents);
    }
}
