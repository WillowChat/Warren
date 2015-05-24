package engineer.carrot.warren.warren.irc.handlers;

import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.warren.IIncomingHandler;
import engineer.carrot.warren.warren.IWarrenDelegate;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.util.IMessageQueue;

import javax.annotation.Nonnull;

public interface IMessageHandler<M extends IMessage> {
    public void setBotDelegate(IWarrenDelegate botInformation);

    public void setOutgoingQueue(IMessageQueue outgoingQueue);

    public void setEventBus(EventBus eventBus);

    public void setIncomingHandler(IIncomingHandler incomingHandler);

    public void handleMessage(@Nonnull M message);
}
