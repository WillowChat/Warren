package engineer.carrot.warren.irc;

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

    public void parseNewUserList(List<String> users) {
        this.users.clear();
        this.userAccessMap.clear();

        for (String userString : users) {
            this.addUser(userString);
        }
    }

    public void removeUser(User user) {
        this.users.remove(user.name);
    }

    public void addUser(String userString) {
        // TODO: Strip supported modes from front based on 005 message in connection setup

        AccessLevel level = AccessLevel.NONE;

        if (userString.startsWith("@") || userString.startsWith("+") || userString.startsWith("~") || userString.startsWith("%")) {
            level = AccessLevel.parseFromIdentifier(userString.charAt(0));
            userString = userString.substring(1);
        }

        User user = new User(userString);
        this.users.put(user.name, user);
        this.userAccessMap.put(user.name, level);
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
