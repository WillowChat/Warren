package engineer.carrot.warren.irc.handlers.RPL;

import engineer.carrot.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.irc.messages.RPL.EndOfMOTDMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

public class EndOfMOTDHandler extends MessageHandler<EndOfMOTDMessage> {
    final Logger LOGGER = LoggerFactory.getLogger(EndOfMOTDHandler.class);

    @Override
    public void handleMessage(@Nonnull EndOfMOTDMessage message) {
        // TODO: Post EndOfMOTD event
        // TODO: Collate MOTD messages in to one, throw a single event for it?

        LOGGER.info("End of MOTD");

        if (this.botDelegate.shouldIdentify()) {
            LOGGER.info("Told to IDENTIFY, sending password");
            this.botDelegate.sendPMToUser("nickserv", "IDENTIFY " + this.botDelegate.getIdentifyPassword());
        }

        List<String> autoJoinChannels = this.botDelegate.getAutoJoinChannels();
        if (!autoJoinChannels.isEmpty()) {
            this.botDelegate.joinChannels(autoJoinChannels);
        }

        this.incomingHandler.setNextExpectedCommandToAnything();
    }
}
