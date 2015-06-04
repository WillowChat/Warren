package engineer.carrot.warren.warren.util;

import engineer.carrot.warren.warren.irc.messages.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageQueue implements IMessageQueue {
    private final Logger LOGGER = LoggerFactory.getLogger(MessageQueue.class);

    private LinkedBlockingQueue<IMessage> queue;

    public MessageQueue() {
        this.initialise();
    }

    private void initialise() {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public void addMessageToQueue(IMessage message) {
        boolean inserted = this.queue.add(message);
        if (!inserted) {
            LOGGER.error("Failed to insert outgoing message in to queue. Expect bad things to happen.");
        }
    }

    @Override
    public IMessage peekQueue() {
        return this.queue.peek();
    }

    @Override
    public IMessage popQueueImmediately() {
        return this.queue.poll();
    }

    @Override
    public IMessage popQueueIndefinitely() throws InterruptedException {
        while (true) {
            IMessage message = this.queue.poll(1000, TimeUnit.MILLISECONDS);
            if (message != null) {
                return message;
            }
        }
    }
}
