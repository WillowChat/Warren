package engineer.carrot.warren.warren.irc.messages.core;

import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import javax.annotation.Nullable;

public class NoticeMessage extends AbstractMessage {
    @Nullable
    private String fromUser;
    private String toTarget;
    private String contents;

    public NoticeMessage() {

    }

    public NoticeMessage(String fromUser, String toTarget, String contents) {
        this.fromUser = fromUser;
        this.toTarget = toTarget;
        this.contents = contents;
    }

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        this.fromUser = message.prefix;
        this.toTarget = message.parameters.get(0);
        this.contents = message.parameters.get(1);
    }

    @Override
    public IrcMessage buildServerOutput() {
        IrcMessage.Builder builder = new IrcMessage.Builder().command(this.getCommandID()).parameters(this.toTarget, this.contents);

        if (this.fromUser != null) {
            builder.prefix(this.fromUser);
        }

        return builder.build();
    }

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        // {"prefix":"server","parameters":["*","contents"],"command":"NOTICE"}
        // {"parameters":["*","contents"],"command":"NOTICE"}
        return message.isParametersExactlyExpectedLength(2);
    }

    @Override
    public String getCommandID() {
        return MessageCodes.NOTICE;
    }
}
