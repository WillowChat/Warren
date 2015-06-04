package engineer.carrot.warren.warren.event;

import engineer.carrot.warren.warren.irc.Channel;

public class TopicNotifyEvent extends Event {
    public final Channel forChannel;
    public final String contents;

    public TopicNotifyEvent(Channel forChannel, String contents) {
        super();

        this.forChannel = forChannel;
        this.contents = contents;
    }
}
