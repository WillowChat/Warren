package engineer.carrot.warren.warren.irc.handlers.core;

import engineer.carrot.warren.warren.event.ServerPingEvent;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.PingMessage;
import engineer.carrot.warren.warren.irc.messages.core.PongMessage;

public class PingHandler extends MessageHandler<PingMessage> {
    @Override
    public void handleMessage(PingMessage message) {
        this.outgoingQueue.addMessageToQueue(new PongMessage(message.pingToken));

        this.eventSink.postEvent(new ServerPingEvent(message.pingToken, message.pingToken));
    }
}
