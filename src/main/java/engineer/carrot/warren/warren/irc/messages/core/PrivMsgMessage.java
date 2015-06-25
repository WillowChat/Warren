package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

public class PrivMsgMessage extends AbstractMessage {
    public String toTarget;
    public String contents;

    public PrivMsgMessage() {

    }

    public PrivMsgMessage(Hostmask fromUser, String toTarget, String contents) {
        this.prefix = fromUser;
        this.toTarget = toTarget;
        this.contents = contents;
    }

    // Inbound

    @Override
    public boolean populate(IrcMessage message) {
        // {"prefix":"otherperson!~op@somehostmask.io","parameters":["MY NICKNAME","private message"],"command":"PRIVMSG"}
        // {"prefix":"beecat!beecat@beecat.","parameters":["#rsspam","channel message"],"command":"PRIVMSG"}

        if (!message.hasPrefix() || message.parameters.size() < 2) {
            return false;
        }

        this.toTarget = message.parameters.get(0);
        this.contents = message.parameters.get(1);

        return true;
    }

    // Outbound

    @Override
    public IrcMessage build() {
        IrcMessage.Builder builder = new IrcMessage.Builder()
                .command(this.getCommand())
                .parameters(this.toTarget, this.contents);

        if (this.prefix != null) {
            builder.prefix(this.prefix.buildOutputString());
        }

        return builder.build();
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.PRIVMSG;
    }
}
