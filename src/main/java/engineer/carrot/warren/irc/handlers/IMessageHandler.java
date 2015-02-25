package engineer.carrot.warren.irc.handlers;

import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.util.IMessageQueue;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.IBotDelegate;

import javax.annotation.Nonnull;

public interface IMessageHandler<M extends IMessage> {
    public void setBotDelegate(IBotDelegate botInformation);

    public void setOutgoingQueue(IMessageQueue outgoingQueue);

    public void setEventBus(EventBus eventBus);

    public void handleMessage(@Nonnull M message);
}
