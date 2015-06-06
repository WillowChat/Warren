package engineer.carrot.warren.warren.irc.messages.RPL;

import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import engineer.carrot.warren.warren.irc.messages.util.ServerTargetContentsMessage;

public class MotdStartMessage extends ServerTargetContentsMessage {
    @Override
    public String getCommandID() {
        return MessageCodes.RPL.MOTDSTART;
    }
}
