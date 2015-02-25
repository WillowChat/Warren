package engineer.carrot.warren.irc.messages.RPL;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.irc.messages.MessageCodes;

import javax.annotation.Nonnull;
import java.util.List;

public class NamReplyMessage implements IMessage {
    public String forServer;
    public String forUser;
    public String channelVisibility;
    public String forChannel;
    public List<String> users;

    @Override
    public boolean isMessageWellFormed(@Nonnull IRCMessage message) {
        // {"prefix":"server","parameters":["nickname","@","#Channel","NormalUser +voiced @op @zsh"],"command":"353"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(4));
    }

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.forServer = message.prefix;
        this.forUser = message.parameters.get(0);
        this.channelVisibility = message.parameters.get(1);
        this.forChannel = message.parameters.get(2);
        this.users = Lists.newArrayList(Splitter.on(' ').split(message.parameters.get(3)));
    }

    @Nonnull
    @Override
    public String getCommandID() {
        return MessageCodes.RPL.NAMREPLY;
    }
}
