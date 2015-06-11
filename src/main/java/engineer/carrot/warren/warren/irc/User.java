package engineer.carrot.warren.warren.irc;

import java.util.Set;

public class User {
    private final Hostmask hostmask;
    private String usernameWithoutAccess;
    private Set<String> prefixes;

    public User(Hostmask hostmask, Set<String> prefixes) {
        this.hostmask = hostmask;
        this.prefixes = prefixes;

        this.computeUsername();
    }

    public void setNickname(String nickname) {
        this.hostmask.user = nickname;
        this.computeUsername();
    }

    private void computeUsername() {
        this.usernameWithoutAccess = this.trimAccess(this.hostmask.user, this.prefixes);
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

    public Hostmask getHostmask() {
        return this.hostmask;
    }

    @Override
    public String toString() {
        return this.hostmask.toString();
    }
}