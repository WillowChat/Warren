package engineer.carrot.warren.event;

import engineer.carrot.warren.irc.Channel;
import engineer.carrot.warren.irc.Hostmask;
import engineer.carrot.warren.irc.User;

public class UserPartedChannelEvent extends Event {
    public User user;
    public Channel channel;
    public String message;

    public UserPartedChannelEvent(User user, Channel channel, String message) {
        super();

        this.user = user;
        this.channel = channel;
        this.message = message;
    }
}
