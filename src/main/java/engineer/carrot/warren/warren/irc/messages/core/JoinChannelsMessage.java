package engineer.carrot.warren.warren.irc.messages.core;

import com.google.common.base.Joiner;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class JoinChannelsMessage extends AbstractMessage {
    private List<String> channels;

    public JoinChannelsMessage(String... channels) {
        this.channels = Arrays.asList(channels);
    }

    public JoinChannelsMessage(List<String> channels) {
        this.channels = channels;
    }

    @Override
    public IRCMessage buildServerOutput() {
        return new IRCMessage.Builder().command(this.getCommandID()).parameters(Joiner.on(",").join(this.channels)).build();
    }

    @Override
    public boolean isMessageWellFormed(@Nonnull IRCMessage message) {
        // {"prefix":"test!~t@test","parameters":["#test"],"command":"JOIN"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(1));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.JOIN;
    }
}
