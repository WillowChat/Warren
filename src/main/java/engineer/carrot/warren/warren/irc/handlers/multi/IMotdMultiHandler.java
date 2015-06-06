package engineer.carrot.warren.warren.irc.handlers.multi;

import engineer.carrot.warren.warren.event.MotdEvent;

public interface IMotdMultiHandler extends IMultiMessageHandler<MotdEvent> {
    void addMotdMessage(String message);

    void setForServer(String forServer);
}
