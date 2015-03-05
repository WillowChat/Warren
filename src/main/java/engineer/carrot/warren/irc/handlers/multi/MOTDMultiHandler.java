package engineer.carrot.warren.irc.handlers.multi;

public class MOTDMultiHandler extends MultiMessageHandler {
    @Override
    public void startConstructing() {
        this.isConstructing = true;


    }

    @Override
    public void finishConstructing() {


        this.isConstructing = false;
    }
}
