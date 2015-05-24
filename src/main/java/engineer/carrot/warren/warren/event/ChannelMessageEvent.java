package engineer.carrot.warren.warren.event;

import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;

public class ChannelMessageEvent extends Event {
    public User fromUser;
    public Channel channel;
    public String contents;

    public ChannelMessageEvent(User fromUser, Channel channel, String contents) {
        super();

        this.fromUser = fromUser;
        this.channel = channel;
        this.contents = contents;
    }
}
