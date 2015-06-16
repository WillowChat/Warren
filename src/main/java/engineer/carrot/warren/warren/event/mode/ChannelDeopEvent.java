package engineer.carrot.warren.warren.event.mode;

import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;

public class ChannelDeopEvent extends ChannelModeEvent {
    public ChannelDeopEvent(User settingUser, User receivingUser, Channel channel) {
        super(settingUser, receivingUser, channel);
    }
}
