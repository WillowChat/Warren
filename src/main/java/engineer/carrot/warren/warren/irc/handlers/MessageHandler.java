package engineer.carrot.warren.warren.irc.handlers;

import engineer.carrot.warren.warren.IEventSink;
import engineer.carrot.warren.warren.IIncomingHandler;
import engineer.carrot.warren.warren.IWarrenDelegate;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.util.IMessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageHandler<M extends IMessage> implements IMessageHandler<M> {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    protected IWarrenDelegate botDelegate;
    protected IMessageQueue outgoingQueue;
    protected IIncomingHandler incomingHandler;
    protected IEventSink eventSink;

    @Override
    public void initialise() {

    }

    @Override
    public void setBotDelegate(IWarrenDelegate botDelegate) {
        this.botDelegate = botDelegate;
    }

    @Override
    public void setEventSink(IEventSink eventSink) {
        this.eventSink = eventSink;
    }

    @Override
    public void setOutgoingQueue(IMessageQueue outgoingQueue) {
        this.outgoingQueue = outgoingQueue;
    }

    @Override
    public void setIncomingHandler(IIncomingHandler incomingHandler) {
        this.incomingHandler = incomingHandler;
    }
}
