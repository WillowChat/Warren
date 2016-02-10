package engineer.carrot.warren.warren.irc;

import com.google.common.collect.Sets;
import engineer.carrot.warren.warren.UserManager;
import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IPrefixSupportModule;

import java.util.Map;
import java.util.Set;

public class Channel {
    public final String name;
    public final Set<User> users;
    public final Map<User, Set<Character>> userModes;
    public final String key;

    private IPrefixSupportModule prefixSupportModule;

    private Channel(Builder builder) {
        this.name = builder.name;
        this.users = builder.users;
        this.userModes = builder.userModes;

        if (builder.key == null) {
            this.key = "";
        } else {
            this.key = builder.key;
        }
    }

    public void setPrefixModule(IPrefixSupportModule prefixSupportModule) {
        this.prefixSupportModule = prefixSupportModule;
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    private void addUser(User user, Set<Character> modes) {
        this.users.add(user);
        this.userModes.put(user, modes);
    }

    public boolean doesUserHaveAtLeastMode(User user, Character mode) {
        if (!this.users.contains(user)) {
            return false;
        }

        Set<Character> usersModes = this.userModes.get(user);
        if (usersModes == null || usersModes.isEmpty()) {
            return false;
        }

        if (usersModes.contains(mode)) {
            return true;
        }

        // Check if the user has a mode greater than this one

        Set<Character> knownModes = this.prefixSupportModule.getModes();
        if (!knownModes.contains(mode)) {
            return false;
        }

        int modePosition = this.prefixSupportModule.getModePosition(mode);

        for (Character cMode : usersModes) {
            if (!knownModes.contains(cMode)) {
                continue;
            }

            int cModePosition = this.prefixSupportModule.getModePosition(cMode);
            if (cModePosition < modePosition) {
                return true;
            }
        }

        return false;
    }

    public boolean addUserMode(User user, Character mode) {
        if (!this.users.contains(user)) {
            return false;
        }

        Set<Character> modes = this.userModes.get(user);
        if (modes == null) {
            return false;
        }

        modes.add(mode);
        return true;
    }

    public boolean removeUserMode(User user, Character mode) {
        if (!this.users.contains(user)) {
            return false;
        }

        Set<Character> modes = this.userModes.get(user);
        if (modes == null || modes.isEmpty()) {
            return false;
        }

        modes.remove(mode);

        return true;
    }

    private boolean containsUser(User user) {
        return this.users.contains(user);
    }

    public User getOrCreateUser(Hostmask hostmask, UserManager userManager) {
        User user = userManager.getOrCreateUser(hostmask);
        if (!this.containsUser(user)) {
            String userString = hostmask.user;
            Character prefix = userString.charAt(0);
            Set<Character> modes = Sets.newHashSet();

            if (this.prefixSupportModule.getPrefixes().contains(prefix)) {
                Character mode = this.prefixSupportModule.getModeFromPrefix(prefix);
                if (mode != null) {
                    modes.add(mode);
                }
            }

            this.addUser(user, modes);
        }

        return user;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static class Builder {
        public String name;
        public Set<User> users;
        public Map<User, Set<Character>> userModes;
        public String key;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder users(Set<User> users) {
            this.users = users;
            return this;
        }

        public Builder userModes(Map<User, Set<Character>> userModes) {
            this.userModes = userModes;
            return this;
        }

        public Channel build() {
            Channel channel = new Channel(this);

            if (channel.name == null) {
                throw new IllegalStateException("Channel must have a name!");
            }

            if (channel.users == null) {
                throw new IllegalStateException("Channel must have users list (even if empty)");
            }

            if (channel.userModes == null) {
                throw new IllegalStateException("Channel must have user modes map (even if empty");
            }

            return channel;
        }
    }
}
