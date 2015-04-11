package engineer.carrot.warren.irc.handlers.multi;

public interface IMultiMessageHandler {
    public void startConstructing();

    public boolean isConstructing();

    public void finishConstructing();
}