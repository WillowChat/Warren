package engineer.carrot.warren;

import com.google.common.collect.Maps;
import engineer.carrot.warren.irc.Hostmask;
import engineer.carrot.warren.irc.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class UserManager {
    private Map<String, User> users;

    public UserManager() {
        this.users = Maps.newHashMap();
    }

    public Map<String, User> getAllUsers() {
        return this.users;
    }

    public boolean containsUser(@Nonnull String userName) {
        return this.users.containsKey(userName);
    }

    @Nullable
    public User getUser(@Nonnull Hostmask hostmask) {
        if (!this.containsUser(hostmask.user)) {
            return null;
        }

        return this.users.get(hostmask.user);
    }

    @Nonnull
    public User getOrCreateUser(@Nonnull Hostmask hostmask) {
        if (!this.containsUser(hostmask.user)) {
            User user = new User(hostmask);
            this.addUser(user);
            return user;
        }

        return this.getUser(hostmask);
    }

    public boolean addUser(@Nonnull User user) {
        if (this.containsUser(user.getName())) {
            return false;
        }

        this.users.put(user.getName(), user);
        return true;
    }

    public void removeUser(@Nonnull String userName) {
        if (this.containsUser(userName)) {
            this.users.remove(userName);
        }
    }
}
