package engineer.carrot.warren.warren;

import com.google.common.collect.Maps;
import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.User;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class UserManager implements IPrefixListener {
    private final Map<String, User> users;
    private Set<Character> prefixes;

    public UserManager(Set<Character> prefixes) {
        this.users = Maps.newHashMap();

        this.prefixesChanged(prefixes);
    }

    public Map<String, User> getAllUsers() {
        return this.users;
    }

    private boolean containsUser(User user) {
        return this.users.containsKey(user.getNameWithoutAccess());
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
            User user = new User(hostmask, this.prefixes);
            this.addUser(user);
            return user;
        }

        return this.getUser(hostmask);
    }

    private boolean addUser(User user) {
        if (this.containsUser(user)) {
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

    private void removeUser(User user) {
        if (this.containsUser(user)) {
            this.removeUser(user.getNameWithoutAccess());
        }
    }

    public boolean renameUser(String oldName, String newName) {
        User user = this.users.get(oldName);
        if (user == null) {
            return false;
        }

        user.setNickname(newName);

        this.removeUser(oldName);
        this.addUser(user);

        return true;
    }

    // IPrefixListener

    @Override
    public void prefixesChanged(Set<Character> prefixes) {
        this.prefixes = prefixes;
    }
}
