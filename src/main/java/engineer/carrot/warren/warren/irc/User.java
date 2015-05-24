package engineer.carrot.warren.warren.irc;

public class User {
    public Hostmask hostmask;
    String usernameWithoutAccess;

    public User(Hostmask hostmask) {
        this.hostmask = hostmask;

        this.usernameWithoutAccess = this.trimAccess(this.hostmask.user);
    }

    private String trimAccess(String user) {
        String trimmedName = this.hostmask.user;
        if (AccessLevel.isKnownIdentifier(trimmedName.charAt(0))) {
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