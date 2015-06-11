package engineer.carrot.warren.warren.event;

import engineer.carrot.warren.warren.irc.User;

public class UserChangedNicknameEvent extends Event {
    public final User user;
    public final String oldNickname;

    public UserChangedNicknameEvent(User user, String oldNickname) {
        super();

        this.user = user;
        this.oldNickname = oldNickname;
    }
}
