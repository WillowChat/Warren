package engineer.carrot.warren.irc.handlers.multi;

public abstract class MultiMessageHandler implements IMultiMessageHandler {
    protected boolean isConstructing = false;

    @Override
    public boolean isConstructing() {
        return this.isConstructing;
    }
}
