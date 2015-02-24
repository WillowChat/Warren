package engineer.carrot.warren.event;

public class ClientJoinedChannelEvent extends Event {
    public String channel;

    public ClientJoinedChannelEvent(String channel) {
        super();

        this.channel = channel;
    }
}
