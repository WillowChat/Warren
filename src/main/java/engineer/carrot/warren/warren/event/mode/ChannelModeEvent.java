package engineer.carrot.warren.warren.event.mode;

import engineer.carrot.warren.warren.event.Event;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;

public abstract class ChannelModeEvent extends Event {
    public final User settingUser;
    public final User receivingUser;
    public final Channel channel;

    public ChannelModeEvent(User settingUser, User receivingUser, Channel channel) {
        super();

        this.settingUser = settingUser;
        this.receivingUser = receivingUser;
        this.channel = channel;
    }
}
