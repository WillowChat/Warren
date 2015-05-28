package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrivMsgMessage extends AbstractMessage {
    @Nullable
    public Hostmask fromUser;
    public String toTarget;
    public String contents;

    public PrivMsgMessage() {

    }

    public PrivMsgMessage(Hostmask fromUser, String toTarget, String contents) {
        this.fromUser = fromUser;
        this.toTarget = toTarget;
        this.contents = contents;
    }

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        // {"prefix":"otherperson!~op@somehostmask.io","parameters":["MY NICKNAME","private message"],"command":"PRIVMSG"}
        // {"prefix":"beecat!beecat@beecat.","parameters":["#rsspam","channel message"],"command":"PRIVMSG"}

        this.fromUser = Hostmask.parseFromString(message.prefix);
        this.toTarget = message.parameters.get(0);
        this.contents = message.parameters.get(1);
    }

    @Override
    public IRCMessage buildServerOutput() {
        IRCMessage.Builder builder = new IRCMessage.Builder().command(this.getCommandID()).parameters(this.toTarget, this.contents);

        if (this.fromUser != null) {
            builder.prefix(this.fromUser.buildOutputString());
        }

        return builder.build();
    }

    @Override
    public boolean isMessageWellFormed(@Nonnull IRCMessage message) {
        // {"prefix":"otherperson!~op@somehostmask.io","parameters":["MY NICKNAME","private message"],"command":"PRIVMSG"}
        // {"prefix":"beecat!beecat@beecat.","parameters":["#rsspam","channel message"],"command":"PRIVMSG"}

        return (message.isPrefixSetAndNotEmpty() && message.isParametersExactlyExpectedLength(2));
    }

    @Override
    public String getCommandID() {
        return MessageCodes.PRIVMSG;
    }
}
