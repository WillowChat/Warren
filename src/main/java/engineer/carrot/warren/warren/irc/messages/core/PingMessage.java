package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class PingMessage extends AbstractMessage {
    public String pingToken;

    public PingMessage() {

    }

    public PingMessage(String pingToken) {
        this.pingToken = pingToken;
    }

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        this.pingToken = message.parameters.get(0);
    }

    @Override
    public IrcMessage buildServerOutput() {
        return new IrcMessage.Builder().command(this.getCommandID()).parameters(this.pingToken).build();
    }

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        // {"command":"PING","parameters":["00BCBDEC"],"tags":{}}
        return message.isParametersExactlyExpectedLength(1);
    }

    @Override
    public String getCommandID() {
        return MessageCodes.PING;
    }
}
