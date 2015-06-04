package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class PingMessage extends AbstractMessage {
    public String pingToken;

    public PingMessage() {

    }

    public PingMessage(String pingToken) {
        this.pingToken = pingToken;
    }

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.pingToken = message.parameters.get(0);
    }

    @Override
    public IRCMessage buildServerOutput() {
        return new IRCMessage.Builder().command(this.getCommandID()).parameters(this.pingToken).build();
    }

    @Override
    public boolean isMessageWellFormed(IRCMessage message) {
        // {"command":"PING","parameters":["00BCBDEC"],"tags":{}}
        return message.isParametersExactlyExpectedLength(1);
    }

    @Override
    public String getCommandID() {
        return MessageCodes.PING;
    }
}
