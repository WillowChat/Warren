package engineer.carrot.warren.warren.event;

import engineer.carrot.warren.warren.irc.User;

public class PrivateMessageEvent extends Event {
    public final User fromUser;
    private final String directedTo;
    public final String contents;

    public PrivateMessageEvent(User fromUser, String directedTo, String contents) {
        super();

        this.fromUser = fromUser;
        this.directedTo = directedTo;
        this.contents = contents;
    }
}
