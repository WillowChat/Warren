package engineer.carrot.warren.irc.handlers;

import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.IIncomingHandler;
import engineer.carrot.warren.IWarrenDelegate;
import engineer.carrot.warren.event.Event;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.util.IMessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageHandler<M extends IMessage> implements IMessageHandler<M> {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    protected IWarrenDelegate botDelegate;
    protected IMessageQueue outgoingQueue;
    protected IIncomingHandler incomingHandler;
    private EventBus eventBus;

    @Override
    public void setBotDelegate(IWarrenDelegate botDelegate) {
        this.botDelegate = botDelegate;
    }

    @Override
    public void setOutgoingQueue(IMessageQueue outgoingQueue) {
        this.outgoingQueue = outgoingQueue;
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void setIncomingHandler(IIncomingHandler incomingHandler) {
        this.incomingHandler = incomingHandler;
    }

    protected void postEvent(Event event) {
        LOGGER.info("Posting framework event: {}", event.getPrettyString());
        this.eventBus.post(event);
    }
}
