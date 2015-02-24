package engineer.carrot.warren.irc.handlers;

import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.event.Event;
import engineer.carrot.warren.IBotDelegate;
import engineer.carrot.warren.IMessageQueue;
import engineer.carrot.warren.irc.messages.IMessage;

public abstract class MessageHandler<M extends IMessage> implements IMessageHandler<M> {
    protected IBotDelegate botDelegate;
    protected IMessageQueue outgoingQueue;
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

    protected void postEvent(Event event) {
        this.eventBus.post(event);
    }
}
