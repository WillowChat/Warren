package engineer.carrot.warren.warren.irc.handlers;

import engineer.carrot.warren.warren.IEventSink;
import engineer.carrot.warren.warren.IIncomingHandler;
import engineer.carrot.warren.warren.IWarrenDelegate;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.util.IMessageQueue;

public interface IMessageHandler<M extends IMessage> {
    void initialise();

    void setBotDelegate(IWarrenDelegate botInformation);

    void setOutgoingQueue(IMessageQueue outgoingQueue);

    void setEventSink(IEventSink eventSink);

    void setIncomingHandler(IIncomingHandler incomingHandler);

    void handleMessage(M message);
}
