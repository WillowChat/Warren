package engineer.carrot.warren.warren;

import com.google.common.base.Optional;
import engineer.carrot.warren.warren.irc.handlers.IMessageHandler;
import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IISupportManager;
import engineer.carrot.warren.warren.irc.handlers.multi.IMotdMultiHandler;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;

import java.util.Set;

public interface IIncomingHandler {
    void setNextExpectedCommands(Set<String> nextExpectedCommands);

    void setNextExpectedCommandToAnything();

    boolean handleIRCMessage(IrcMessage message, String originalLine);

    IMotdMultiHandler getMotdHandler();

    IISupportManager getISupportManager();

    // API

    void addMessageHandler(IMessage message, Optional<IMessageHandler> handler);
}
