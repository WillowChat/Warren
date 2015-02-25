package engineer.carrot.warren.event;

import engineer.carrot.warren.irc.Hostmask;

public class UserJoinedChannelEvent extends Event {
    public Hostmask user;
    public String channel;

    public UserJoinedChannelEvent(Hostmask user, String channel) {
        super();

        this.user = user;
        this.channel = channel;
    }
}
