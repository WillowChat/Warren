package engineer.carrot.warren.irc.messages.core;

import engineer.carrot.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.irc.messages.IMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoticeMessage implements IMessage {
    @Nullable
    public String fromUser;
    public String toTarget;
    public String contents;

    public NoticeMessage() {

    }

    public NoticeMessage(String fromUser, String toTarget, String contents) {
        this.fromUser = fromUser;
        this.toTarget = toTarget;
        this.contents = contents;
    }

    @Override
    public void populateFromIRCMessage(IRCMessage message) {
        this.fromUser = message.prefix;
        this.toTarget = message.parameters.get(0);
        this.contents = message.parameters.get(1);
    }

    @Override
    public IRCMessage buildServerOutput() {
        IRCMessage.Builder builder = new IRCMessage.Builder().command(this.getCommandID()).parameters(this.toTarget, this.contents);

        if (this.fromUser != null) {
            builder.prefix(this.fromUser);
        }

        return builder.build();
    }

    @Override
    public boolean isMessageWellFormed(@Nonnull IRCMessage message) {
        // {"prefix":"server","parameters":["*","contents"],"command":"NOTICE"}
        // {"parameters":["*","contents"],"command":"NOTICE"}
        return message.isParametersExactlyExpectedLength(2);
    }

    @Override
    public String getCommandID() {
        return MessageCodes.NOTICE;
    }
}
