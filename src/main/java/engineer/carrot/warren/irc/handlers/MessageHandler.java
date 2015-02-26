package engineer.carrot.warren.irc.handlers;

import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.IBotDelegate;
import engineer.carrot.warren.IIncomingHandler;
import engineer.carrot.warren.event.Event;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.util.IMessageQueue;

public abstract class MessageHandler<M extends IMessage> implements IMessageHandler<M> {
    protected IBotDelegate botDelegate;
    protected IMessageQueue outgoingQueue;
    protected IIncomingHandler incomingHandler;
    private EventBus eventBus;

    @Override
    public void setBotDelegate(IBotDelegate botDelegate) {
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
        this.eventBus.post(event);
    }
}
