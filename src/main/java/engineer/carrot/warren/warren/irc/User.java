package engineer.carrot.warren.warren.irc;

import java.util.Set;

public class User {
    private final Hostmask hostmask;
    private final String usernameWithoutAccess;

    public User(Hostmask hostmask, Set<String> prefixes) {
        this.hostmask = hostmask;

        this.usernameWithoutAccess = this.trimAccess(this.hostmask.user, prefixes);
    }

    private String trimAccess(String user, Set<String> prefixes) {
        String trimmedName = user;
        if (prefixes.contains(Character.toString(trimmedName.charAt(0)))) {
            trimmedName = trimmedName.substring(1);
        }

        return trimmedName;
    }

    public String getNameWithoutAccess() {
        return this.usernameWithoutAccess;
    }

    public String getNameWithAccess() {
        return this.hostmask.user;
    }
}