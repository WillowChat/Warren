package engineer.carrot.warren.warren.irc.messages.RPL;

import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.warren.irc.messages.util.ServerTargetContentsMessage;

import javax.annotation.Nonnull;

public class MOTDMessage extends ServerTargetContentsMessage {
    @Nonnull
    @Override
    public String getCommandID() {
        return MessageCodes.RPL.MOTD;
    }
}
