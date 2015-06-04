package engineer.carrot.warren.warren.event;

import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;

public class UserPartedChannelEvent extends Event {
    public final User user;
    public final Channel channel;
    public final String message;

    public UserPartedChannelEvent(User user, Channel channel, String message) {
        super();

        this.user = user;
        this.channel = channel;
        this.message = message;
    }
}
