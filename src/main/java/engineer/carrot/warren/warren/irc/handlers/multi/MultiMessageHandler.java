package engineer.carrot.warren.warren.irc.handlers.multi;

public abstract class MultiMessageHandler implements IMultiMessageHandler {
    boolean isConstructing = false;

    @Override
    public boolean isConstructing() {
        return this.isConstructing;
    }
}
