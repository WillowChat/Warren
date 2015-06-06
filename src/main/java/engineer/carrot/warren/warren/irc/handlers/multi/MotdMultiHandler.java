package engineer.carrot.warren.warren.irc.handlers.multi;

import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.event.MotdEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MotdMultiHandler extends MultiMessageHandler<MotdEvent> implements IMotdMultiHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(MotdMultiHandler.class);

    private List<String> motd = Lists.newArrayList();
    private String forServer = "";

    @Override
    public void addMotdMessage(String message) {
        motd.add(message);
    }

    @Override
    public void setForServer(String forServer) {
        this.forServer = forServer;
    }

    @Override
    protected void initialiseHandler() {
        this.motd.clear();
        this.forServer = "";
    }

    @Override
    protected MotdEvent formResult() {
        return new MotdEvent(this.motd, this.forServer);
    }
}
