package engineer.carrot.warren.warren.event;

import java.util.List;

public class MotdEvent extends Event {
    public final List<String> motd;
    public String forServer;

    public MotdEvent(List<String> motd, String forServer) {
        super();

        this.motd = motd;
        this.forServer = forServer;
    }
}
