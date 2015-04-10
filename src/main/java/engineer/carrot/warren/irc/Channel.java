package engineer.carrot.warren.irc;

import engineer.carrot.warren.UserManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class Channel {
    @Nonnull
    public String name;
    @Nonnull
    public Map<String, User> users;
    @Nonnull
    public Map<String, AccessLevel> userAccessMap;

    private Channel(Builder builder) {
        this.name = builder.name;
        this.users = builder.users;
        this.userAccessMap = builder.userAccessMap;
    }

    public void removeUser(User user) {
        this.users.remove(user.getName());
    }

    public void addUser(User user, AccessLevel level) {
        this.users.put(user.getName(), user);
        this.userAccessMap.put(user.getName(), level);
    }

    public boolean containsUser(User user) {
        return this.users.containsKey(user.getName());
    }

    @Nonnull
    public User getOrCreateUser(Hostmask hostmask, UserManager userManager) {
        User user = userManager.getOrCreateUser(hostmask);
        if (!this.containsUser(user)) {
            AccessLevel level = AccessLevel.NONE;

            String userString = hostmask.user;
            char possibleIdentifier = userString.charAt(0);
            if (AccessLevel.isKnownIdentifier(possibleIdentifier)) {
                level = AccessLevel.parseFromIdentifier(possibleIdentifier);

                // Trim access modifier from username
                user.hostmask.user = user.hostmask.user.substring(1);
            }

            this.addUser(user, level);
        }

        return user;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static class Builder {
        public String name;
        public Map<String, User> users;
        public Map<String, AccessLevel> userAccessMap;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder users(Map<String, User> users) {
            this.users = users;
            return this;
        }

        public Builder userAccessMap(Map<String, AccessLevel> userAccessMap) {
            this.userAccessMap = userAccessMap;
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

            if (channel.userAccessMap == null) {
                throw new IllegalStateException("Channel must have user access map (even if empty");
            }

            return channel;
        }
    }
}
