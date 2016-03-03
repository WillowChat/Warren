package engineer.carrot.warren.warren.irc.messages;

import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IISupportManager;
import engineer.carrot.warren.warren.irc.messages.util.NoOpException;

import javax.annotation.Nullable;

public abstract class AbstractMessage implements IMessage {
    protected transient IISupportManager iSupportManager;

    @Nullable
    public Hostmask prefix;

    @Override
    public void setISupportManager(IISupportManager iSupportManager) {
        this.iSupportManager = iSupportManager;
    }

    // Inbound

    @Override
    public IMessage build(JavaIrcMessage message) {
        if (message.hasPrefix()) {
            this.prefix = Hostmask.parseFromString(message.prefix);
        }

        boolean success = this.populate(message);
        if (!success) {
            throw new RuntimeException("Failed to populate from message: " + message);
        }

        return this;
    }

    @Override
    public boolean populate(JavaIrcMessage message) {
        return false;
    }

    // Outbound

    @Override
    public JavaIrcMessage build() {
        throw new NoOpException();
    }
}
