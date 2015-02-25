package engineer.carrot.warren.event;

import engineer.carrot.warren.irc.Hostmask;

public class UserPartedChannelEvent extends Event {
    public Hostmask user;
    public String channel;
    public String message;

    public UserPartedChannelEvent(Hostmask user, String channel, String message) {
        super();

        this.user = user;
        this.channel = channel;
        this.message = message;
    }
}
