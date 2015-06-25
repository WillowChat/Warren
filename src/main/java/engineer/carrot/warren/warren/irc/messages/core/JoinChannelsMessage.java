package engineer.carrot.warren.warren.irc.messages.core;

import com.google.common.base.Joiner;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import java.util.List;

public class JoinChannelsMessage extends AbstractMessage {
    private final List<String> channels;

    public JoinChannelsMessage(List<String> channels) {
        this.channels = channels;
    }

    // Outbound

    @Override
    public IrcMessage build() {
        return new IrcMessage.Builder()
                .command(this.getCommand())
                .parameters(Joiner.on(",").join(this.channels))
                .build();
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.JOIN;
    }
}
