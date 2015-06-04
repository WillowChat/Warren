package engineer.carrot.warren.warren.irc.handlers.RPL;

import engineer.carrot.warren.warren.event.EndOfMOTDEvent;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.RPL.EndOfMOTDMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EndOfMOTDHandler extends MessageHandler<EndOfMOTDMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(EndOfMOTDHandler.class);

    @Override
    public void handleMessage(EndOfMOTDMessage message) {
        // TODO: Post EndOfMOTD event
        // TODO: Collate MOTD messages in to one, throw a single  event for it?

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

        this.postEvent(new EndOfMOTDEvent());
    }
}
