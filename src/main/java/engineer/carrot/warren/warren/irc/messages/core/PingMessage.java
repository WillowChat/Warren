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

    // Inbound

    @Override
    public boolean populate(IrcMessage message) {
        // {"command":"PING","parameters":["00BCBDEC"],"tags":{}}

        if (!message.hasParameters()) {
            return false;
        }

        this.pingToken = message.parameters.get(0);

        return true;
    }

    // Outbound

    @Override
    public IrcMessage build() {
        return new IrcMessage.Builder()
                .command(this.getCommand())
                .parameters(this.pingToken)
                .build();
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.PING;
    }
}
