package engineer.carrot.warren.event;

import engineer.carrot.warren.irc.Channel;

public class ClientJoinedChannelEvent extends Event {
    public Channel channel;

    public ClientJoinedChannelEvent(Channel channel) {
        super();

        this.channel = channel;
    }
}
