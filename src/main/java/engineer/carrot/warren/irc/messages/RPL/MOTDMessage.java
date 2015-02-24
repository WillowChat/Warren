package engineer.carrot.warren.irc.messages.RPL;

import engineer.carrot.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.irc.messages.util.ServerTargetContentsMessage;

import javax.annotation.Nonnull;

public class MOTDMessage extends ServerTargetContentsMessage {
    @Nonnull
    @Override
    public String getCommandID() {
        return MessageCodes.RPL.MOTD;
    }
}
