package engineer.carrot.warren.warren.irc.handlers;

import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.warren.IIncomingHandler;
import engineer.carrot.warren.warren.IWarrenDelegate;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.util.IMessageQueue;

public interface IMessageHandler<M extends IMessage> {
    void setBotDelegate(IWarrenDelegate botInformation);

    void setOutgoingQueue(IMessageQueue outgoingQueue);

    void setEventBus(EventBus eventBus);

    void setIncomingHandler(IIncomingHandler incomingHandler);

    void handleMessage(M message);
}
