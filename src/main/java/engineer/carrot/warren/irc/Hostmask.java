package engineer.carrot.warren.irc;

import com.google.common.base.Strings;
import engineer.carrot.warren.irc.messages.IRCMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Hostmask {
    @Nonnull
    public String user;
    @Nullable
    public String host;
    @Nullable
    public String server;

    public static final int MAX_LENGTH = IRCMessage.MAX_LENGTH;
    public static final int MIN_LENGTH = IRCMessage.MIN_LENGTH;

    private Hostmask(Builder builder) {
        this.user = builder.user;
        this.host = builder.host;
        this.server = builder.server;
    }

    @Nullable
    public static Hostmask parseFromString(String hostmask) {
        // "Test!~test@test.domain"
        int length = hostmask.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            return null;
        }

        Builder builder = new Builder();

        int currentPosition = 0;

        int exclamPosition = hostmask.indexOf(CharacterCodes.EXCLAM);
        if (exclamPosition > 0) {
            String user = hostmask.substring(currentPosition, exclamPosition);
            builder.user(user);

            currentPosition = exclamPosition + 1;
        }

        int atPosition = hostmask.indexOf(CharacterCodes.AT, currentPosition);
        if (atPosition > 0) {
            String host = hostmask.substring(currentPosition, atPosition);
            builder.host(host);

            currentPosition = atPosition + 1;

            if (currentPosition < length) {
                String server = hostmask.substring(currentPosition);
                builder.server(server);
            }
        }

        return builder.build();
    }

    public boolean containsUser() {
        return !Strings.isNullOrEmpty(this.user);
    }

    public boolean containsHost() {
        return !Strings.isNullOrEmpty(this.host);
    }

    public boolean containsServer() {
        return !Strings.isNullOrEmpty(this.server);
    }

    public String buildOutputString() {
        if (!this.containsUser()) {
            throw new IllegalStateException("Cannot build Hostmask without a User");
        }

        if (this.containsHost() && this.containsServer()) {
            return this.user + CharacterCodes.EXCLAM + this.host + CharacterCodes.AT + this.server;
        } else {
            return this.user;
        }
    }

    @Override
    public String toString() {
        // "Test!~test@test.domain"
        if (!this.containsHost() || !this.containsServer()) {
            return this.user;
        } else {
            return this.user + "!" + this.host + "@" + this.server;
        }
    }

    public static class Builder {
        public String user = null;
        public String host = null;
        public String server = null;

        public Builder() {

        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder server(String server) {
            this.server = server;
            return this;
        }

        public Hostmask build() {
            Hostmask hostmask = new Hostmask(this);
            if (hostmask.user == null) {
                throw new IllegalStateException("User in hostmask not set!");
            }

            return hostmask;
        }
    }
}
