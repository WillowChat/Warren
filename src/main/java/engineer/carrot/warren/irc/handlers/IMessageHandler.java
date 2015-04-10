package engineer.carrot.warren.irc.handlers;

import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.IWarrenDelegate;
import engineer.carrot.warren.IIncomingHandler;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.util.IMessageQueue;

import javax.annotation.Nonnull;

public interface IMessageHandler<M extends IMessage> {
    public void setBotDelegate(IWarrenDelegate botInformation);

    public void setOutgoingQueue(IMessageQueue outgoingQueue);

    public void setEventBus(EventBus eventBus);

    public void setIncomingHandler(IIncomingHandler incomingHandler);

    public void handleMessage(@Nonnull M message);
}
