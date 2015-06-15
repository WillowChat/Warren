package engineer.carrot.warren.warren.irc.messages;

import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IISupportManager;
import engineer.carrot.warren.warren.irc.messages.util.NoOpException;

public abstract class AbstractMessage implements IMessage {
    protected transient IISupportManager iSupportManager;

    @Override
    public void setISupportManager(IISupportManager iSupportManager) {
        this.iSupportManager = iSupportManager;
    }

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        throw new NoOpException();
    }

    @Override
    public IMessage build(IrcMessage message) {
        this.populateFromIRCMessage(message);

        return this;
    }

    @Override
    public IrcMessage buildServerOutput() {
        throw new NoOpException();
    }
}
