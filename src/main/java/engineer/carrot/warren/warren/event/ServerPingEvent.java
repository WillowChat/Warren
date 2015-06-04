package engineer.carrot.warren.warren.event;

public class ServerPingEvent extends Event {
    public final String pingToken;
    public final String pongToken;

    public ServerPingEvent(String pingToken, String pongToken) {
        super();

        this.pingToken = pingToken;
        this.pongToken = pongToken;
    }
}
