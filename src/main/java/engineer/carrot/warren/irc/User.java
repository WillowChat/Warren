package engineer.carrot.warren.irc;

public class User {
    public Hostmask hostmask;

    public User(Hostmask hostmask) {
        this.hostmask = hostmask;
    }

    public String getName() {
        return this.hostmask.user;
    }
}