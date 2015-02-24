package engineer.carrot.warren.event;

public class ServerPingEvent extends Event {
    public String pingToken;
    public String pongToken;

    public ServerPingEvent(String pingToken, String pongToken) {
        super();

        this.pingToken = pingToken;
        this.pongToken = pongToken;
    }
}
