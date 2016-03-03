package engineer.carrot.warren.warren.irc.messages.util;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;

public abstract class ServerTargetContentsMessage extends AbstractMessage {
    public String forServer;
    public String toTarget;
    public String contents;

    // Inbound

    @Override
    public boolean populate(JavaIrcMessage message) {
        if (!message.hasPrefix() || message.parameters.size() < 2) {
            return false;
        }

        this.forServer = message.prefix;
        this.toTarget = message.parameters.get(0);
        this.contents = message.parameters.get(1);

        return true;
    }
}
