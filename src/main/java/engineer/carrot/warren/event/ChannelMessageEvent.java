package engineer.carrot.warren.event;

import engineer.carrot.warren.irc.Hostmask;

public class ChannelMessageEvent extends Event {
    public Hostmask fromUser;
    public String channel;
    public String contents;

    public ChannelMessageEvent(Hostmask fromUser, String channel, String contents) {
        super();

        this.fromUser = fromUser;
        this.channel = channel;
        this.contents = contents;
    }
}
