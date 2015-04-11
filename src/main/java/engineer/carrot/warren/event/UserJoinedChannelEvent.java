package engineer.carrot.warren.event;

import engineer.carrot.warren.irc.Channel;
import engineer.carrot.warren.irc.User;

public class UserJoinedChannelEvent extends Event {
    public User user;
    public Channel channel;

    public UserJoinedChannelEvent(User user, Channel channel) {
        super();

        this.user = user;
        this.channel = channel;
    }
}
