package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class PongMessage extends AbstractMessage {
    private String pongAuthor;
    private String pongToken;

    public PongMessage() {

    }

    public PongMessage(String pongToken) {
        this.pongToken = pongToken;
    }

    // Inbound

    @Override
    public boolean populate(IrcMessage message) {
        if (message.parameters.size() < 2) {
            return false;
        }

        this.pongAuthor = message.parameters.get(0);
        this.pongToken = message.parameters.get(1);

        return true;
    }

    // Outbound

    @Override
    public IrcMessage build() {
        return new IrcMessage.Builder()
                .command(this.getCommand())
                .parameters(this.pongToken)
                .build();
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.PONG;
    }
}
