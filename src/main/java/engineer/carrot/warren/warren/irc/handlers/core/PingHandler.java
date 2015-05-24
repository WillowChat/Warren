package engineer.carrot.warren.warren.irc.handlers.core;

import engineer.carrot.warren.warren.event.ServerPingEvent;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.PingMessage;
import engineer.carrot.warren.warren.irc.messages.core.PongMessage;

import javax.annotation.Nonnull;

public class PingHandler extends MessageHandler<PingMessage> {
    @Override
    public void handleMessage(@Nonnull PingMessage message) {
        this.outgoingQueue.addMessageToQueue(new PongMessage(message.pingToken));

        this.postEvent(new ServerPingEvent(message.pingToken, message.pingToken));
    }
}
