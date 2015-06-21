package engineer.carrot.warren.warren.util;

import engineer.carrot.warren.warren.irc.messages.IMessage;

import javax.annotation.Nullable;

public interface IMessageQueue {
    void addMessage(IMessage message);

    @Nullable
    IMessage peek();

    @Nullable
    IMessage popImmediately();

    @Nullable
    IMessage popIndefinitely() throws InterruptedException;
}
