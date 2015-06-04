package engineer.carrot.warren.warren.event;

import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;

public class UserJoinedChannelEvent extends Event {
    public final User user;
    public final Channel channel;

    public UserJoinedChannelEvent(User user, Channel channel) {
        super();

        this.user = user;
        this.channel = channel;
    }
}
