package engineer.carrot.warren.warren.irc.handlers.core.mode;

import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.messages.core.ModeMessage;

public interface IModeHandlerModule {
    boolean handleModeChange(ModeMessage.ModeModifier modifier, Channel channel);
}
