package engineer.carrot.warren.warren.irc.handlers.multi;

interface IMultiMessageHandler {
    public void startConstructing();

    public boolean isConstructing();

    public void finishConstructing();
}