package engineer.carrot.warren.warren.irc.handlers.RPL;

import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.RPL.ISupportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;

public class ISupportHandler extends MessageHandler<ISupportMessage> {
    final Logger LOGGER = LoggerFactory.getLogger(ISupportHandler.class);

    @Override
    public void handleMessage(@Nonnull ISupportMessage message) {
        LOGGER.info("Server supports: ");

        for (Map.Entry<String, String> entry : message.parameters.entrySet()) {
            LOGGER.info("{}: {}", entry.getKey(), entry.getValue());
        }
    }
}
