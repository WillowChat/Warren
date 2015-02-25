package engineer.carrot.warren.util;

import engineer.carrot.warren.irc.messages.IMessage;

import javax.annotation.Nullable;

public interface IMessageQueue {
    public void addMessageToQueue(IMessage message);

    @Nullable
    public IMessage peekQueue();

    @Nullable
    public IMessage popQueueImmediately();

    @Nullable
    public IMessage popQueueIndefinitely() throws InterruptedException;
}
