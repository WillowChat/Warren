package engineer.carrot.warren.event;

import engineer.carrot.warren.irc.User;

public class PrivateMessageEvent extends Event {
    public User fromUser;
    public String directedTo;
    public String contents;

    public PrivateMessageEvent(User fromUser, String directedTo, String contents) {
        super();

        this.fromUser = fromUser;
        this.directedTo = directedTo;
        this.contents = contents;
    }
}
