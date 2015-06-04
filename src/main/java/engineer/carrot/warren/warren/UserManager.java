package engineer.carrot.warren.warren;

import com.google.common.collect.Maps;
import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.User;

import javax.annotation.Nullable;
import java.util.Map;

public class UserManager {
    private final Map<String, User> users;

    public UserManager() {
        this.users = Maps.newHashMap();
    }

    public Map<String, User> getAllUsers() {
        return this.users;
    }

    private boolean containsUser(String userName) {
        return this.users.containsKey(userName);
    }

    @Nullable
    private User getUser(Hostmask hostmask) {
        if (!this.containsUser(hostmask.user)) {
            return null;
        }

        return this.users.get(hostmask.user);
    }

    public User getOrCreateUser(Hostmask hostmask) {
        if (!this.containsUser(hostmask.user)) {
            User user = new User(hostmask);
            this.addUser(user);
            return user;
        }

        return this.getUser(hostmask);
    }

    private boolean addUser(User user) {
        if (this.containsUser(user.getNameWithoutAccess())) {
            return false;
        }

        this.users.put(user.getNameWithoutAccess(), user);
        return true;
    }

    public void removeUser(String userName) {
        if (this.containsUser(userName)) {
            this.users.remove(userName);
        }
    }
}
