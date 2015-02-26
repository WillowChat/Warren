package engineer.carrot.warren;

import engineer.carrot.warren.irc.messages.IRCMessage;

import java.util.Set;

public interface IIncomingHandler {
    public void setNextExpectedCommands(Set<String> nextExpectedCommands);

    public void setNextExpectedCommandToAnything();

    public boolean handleIRCMessage(IRCMessage message, String originalLine);
}
