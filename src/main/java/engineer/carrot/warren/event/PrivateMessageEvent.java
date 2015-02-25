package engineer.carrot.warren.event;

import engineer.carrot.warren.irc.Hostmask;

public class PrivateMessageEvent extends Event {
    public Hostmask fromUser;
    public String directedTo;
    public String contents;

    public PrivateMessageEvent(Hostmask fromUser, String directedTo, String contents) {
        super();

        this.fromUser = fromUser;
        this.directedTo = directedTo;
        this.contents = contents;
    }
}
