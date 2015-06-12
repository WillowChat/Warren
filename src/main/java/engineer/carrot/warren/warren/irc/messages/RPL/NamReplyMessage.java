package engineer.carrot.warren.warren.irc.messages.RPL;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import java.util.List;

public class NamReplyMessage extends AbstractMessage {
    private String forServer;
    public String forUser;
    private String channelVisibility;
    public String forChannel;
    public List<Hostmask> hostmasks;

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        // {"prefix":"server","parameters":["nickname","@","#Channel","NormalUser +voiced @op @zsh"],"command":"353"}
        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(4));
    }

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        this.forServer = message.prefix;
        this.forUser = message.parameters.get(0);
        this.channelVisibility = message.parameters.get(1);
        this.forChannel = message.parameters.get(2);

        List<String> names = Lists.newArrayList(Splitter.on(' ').split(message.parameters.get(3)));
        List<Hostmask> hostmasks = Lists.newArrayList();

        for (String name : names) {
            hostmasks.add(new Hostmask.Builder().user(name).build());
        }

        this.hostmasks = hostmasks;
    }

    @Override
    public String getCommandID() {
        return MessageCodes.RPL.NAMREPLY;
    }
}
