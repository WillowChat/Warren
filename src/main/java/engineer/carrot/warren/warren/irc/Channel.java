package engineer.carrot.warren.warren.irc;

import engineer.carrot.warren.warren.UserManager;

import java.util.Map;
import java.util.Set;

public class Channel {
    public final String name;
    public final Set<User> users;
    public final Map<User, AccessLevel> userAccessMap;

    private Channel(Builder builder) {
        this.name = builder.name;
        this.users = builder.users;
        this.userAccessMap = builder.userAccessMap;
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    private void addUser(User user, AccessLevel level) {
        this.users.add(user);
        this.userAccessMap.put(user, level);
    }

    public boolean doesUserHaveAccessLevel(User user, AccessLevel accessLevel) {
        if (!this.users.contains(user)) {
            return false;
        }

        AccessLevel level = this.userAccessMap.getOrDefault(user, AccessLevel.NONE);
        return (level == accessLevel);
    }

    public boolean setUserAccessLevel(User user, AccessLevel accessLevel) {
        if (!this.users.contains(user)) {
            return false;
        }

        this.userAccessMap.put(user, accessLevel);
        return true;
    }

    private boolean containsUser(User user) {
        return this.users.contains(user);
    }

    public User getOrCreateUser(Hostmask hostmask, UserManager userManager) {
        User user = userManager.getOrCreateUser(hostmask);
        if (!this.containsUser(user)) {
            AccessLevel level = AccessLevel.NONE;

            String userString = hostmask.user;
            char possibleIdentifier = userString.charAt(0);
            if (AccessLevel.isKnownIdentifier(possibleIdentifier)) {
                level = AccessLevel.parseFromIdentifier(possibleIdentifier);
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
        public Set<User> users;
        public Map<User, AccessLevel> userAccessMap;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder users(Set<User> users) {
            this.users = users;
            return this;
        }

        public Builder userAccessMap(Map<User, AccessLevel> userAccessMap) {
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
