package engineer.carrot.warren.warren.irc.handlers.RPL;

import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.event.MotdEvent;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.handlers.multi.IMotdMultiHandler;
import engineer.carrot.warren.warren.irc.messages.RPL.MotdEndMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MotdEndHandler extends MessageHandler<MotdEndMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(MotdEndHandler.class);

    @Override
    public void handleMessage(MotdEndMessage message) {
        IMotdMultiHandler handler = this.incomingHandler.getMotdHandler();
        MotdEvent event = null;

        if (!handler.isConstructing()) {
            LOGGER.warn("MOTD handler doesn't think it's constructing an MOTD - it will be blank");
        } else {
            event = handler.finishConstructing();
        }

        if (event == null) {
            event = new MotdEvent(Lists.<String>newArrayList(), message.forServer);
        }

        if (this.botDelegate.shouldIdentify()) {
            LOGGER.info("Told to IDENTIFY, sending password");
            this.botDelegate.sendPMToUser("nickserv", "IDENTIFY " + this.botDelegate.getIdentifyPassword());

            // TODO: Workaround! Wait 5 seconds before trying to join channels
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOGGER.error("Sleep for NickServ interrupted");
            }
        }

        List<String> autoJoinChannels = this.botDelegate.getAutoJoinChannels();
        if (!autoJoinChannels.isEmpty()) {
            this.botDelegate.joinChannels(autoJoinChannels);
        }

        this.incomingHandler.setNextExpectedCommandToAnything();

        this.eventSink.postEvent(event);
    }
}
