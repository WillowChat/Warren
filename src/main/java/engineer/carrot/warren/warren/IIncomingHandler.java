package engineer.carrot.warren.warren;

import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.ISupportHandler;
import engineer.carrot.warren.warren.irc.handlers.multi.IMotdMultiHandler;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;

import java.util.Set;

public interface IIncomingHandler {
    void setNextExpectedCommands(Set<String> nextExpectedCommands);

    void setNextExpectedCommandToAnything();

    boolean handleIRCMessage(IRCMessage message, String originalLine);

    IMotdMultiHandler getMotdHandler();

    ISupportHandler getISupportHandler();
}
