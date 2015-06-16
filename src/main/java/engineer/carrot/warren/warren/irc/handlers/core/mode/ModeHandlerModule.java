package engineer.carrot.warren.warren.irc.handlers.core.mode;

import engineer.carrot.warren.warren.IEventSink;
import engineer.carrot.warren.warren.UserManager;

public abstract class ModeHandlerModule implements IModeHandlerModule {
    protected IEventSink eventSink;
    protected UserManager userManager;

    public ModeHandlerModule(IEventSink eventSink, UserManager userManager) {
        this.eventSink = eventSink;
        this.userManager = userManager;
    }
}
