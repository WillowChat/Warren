package engineer.carrot.warren.warren.irc.messages;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import engineer.carrot.warren.warren.irc.CharacterCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class IRCMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(IRCMessage.class);

    @Nullable
    private final Map<String, String> tags;
    @Nullable
    public final String prefix;
    @Nullable
    public final List<String> parameters;

    public final String command;

    public static final int MAX_LENGTH = 510;
    public static final int MIN_LENGTH = 3;

    private IRCMessage(Builder builder) {
        this.tags = builder.tags;
        this.prefix = builder.prefix;
        this.command = builder.command;
        this.parameters = builder.parameters;
    }

    public String buildPrettyOutput() {
        return new Gson().toJson(this);
    }

    @Nullable
    public static IRCMessage parseFromLine(String line) {
        int length = line.length();
        if (length > MAX_LENGTH || length < MIN_LENGTH) {
            return null;
        }

        Builder builder = new Builder();

        int stringPosition = 0;

        // http://ircv3.atheme.org/specification/message-tags-3.2

        // Tags
        if (line.charAt(stringPosition) == CharacterCodes.AT) {
            int nextSpace = line.indexOf(CharacterCodes.SPACE, stringPosition);
            if (nextSpace < 0) {
                LOGGER.error("Malformed tag: no trailing space found");
                return null;
            }

            Map<String, String> tags = Maps.newHashMap();

            String[] tagsArray = line.substring(1, nextSpace).split(String.valueOf(CharacterCodes.SEMICOLON));
            for (String tag : tagsArray) {
                String[] splitTag = tag.split(String.valueOf(CharacterCodes.EQUALS), 2);
                if (splitTag.length < 2) {
                    LOGGER.error("Malformed tag: no key or value");
                    return null;
                }

                tags.put(splitTag[0], splitTag[1]);
            }

            builder.tags(tags);

            stringPosition = nextSpace + 1;

            // Trim trailing spaces (some IRC daemons had this)
            while (line.charAt(stringPosition) == CharacterCodes.SPACE) {
                stringPosition++;
            }
        }

        // Prefix
        if (line.charAt(stringPosition) == CharacterCodes.COLON) {
            int nextSpace = line.indexOf(CharacterCodes.SPACE, stringPosition);
            if (nextSpace < 0) {
                LOGGER.error("Malformed prefix: no trailing space found");
                return null;
            }

            builder.prefix(line.substring(stringPosition + 1, nextSpace));

            stringPosition = nextSpace + 1;

            // Trim trailing spaces (some IRC daemons had this)
            while (line.charAt(stringPosition) == CharacterCodes.SPACE) {
                stringPosition++;
            }
        }

        // Command
        int nextSpace = line.indexOf(CharacterCodes.SPACE, stringPosition);
        if (nextSpace < 0) {
            // No parameters after the command, so everything left is the command
            String command = line.substring(stringPosition);
            if (command.length() > 0) {
                builder.command(command);
                return builder.build();
            } else {
                LOGGER.error("Malformed command: zero length");
                return null;
            }
        } else {
            builder.command(line.substring(stringPosition, nextSpace));

            stringPosition = nextSpace + 1;
        }

        // Trim trailing spaces (some IRC daemons had this)
        while (line.charAt(stringPosition) == CharacterCodes.SPACE) {
            stringPosition++;
        }

        // Parameters

        List<String> parameters = Lists.newArrayList();

        while (stringPosition < length) {
            if (line.charAt(stringPosition) == CharacterCodes.COLON) {
                // Rest of the message is one parameter

                String finalParam = line.substring(stringPosition + 1);
                parameters.add(finalParam);

                return builder.parameters(parameters).build();
            }

            nextSpace = line.indexOf(CharacterCodes.SPACE, stringPosition);
            if (nextSpace >= 0) {
                String param = line.substring(stringPosition, nextSpace);
                parameters.add(param);
                stringPosition = nextSpace + 1;

                // Trim trailing spaces (some IRC daemons had this)
                while (line.charAt(stringPosition) == CharacterCodes.SPACE) {
                    stringPosition++;
                }

                continue;
            }

            if (nextSpace < 0) {
                String param = line.substring(stringPosition);
                parameters.add(param);

                break;
            }
        }

        if (!parameters.isEmpty()) {
            builder.parameters(parameters);
        }

        return builder.build();
    }

    public boolean isPrefixSet() {
        return this.prefix != null;
    }

    public boolean isPrefixSetAndNotEmpty() {
        return !Strings.isNullOrEmpty(this.prefix);
    }

    public boolean isTagsSet() {
        return this.tags != null;
    }

    public boolean isCommandSet() {
        return this.command != null;
    }

    public boolean isParametersSet() {
        return this.parameters != null;
    }

    public boolean isParametersAtLeastExpectedLength(int expectedLength) {
        if (!this.isParametersSet()) {
            return false;
        }

        if (this.parameters.size() < expectedLength) {
            return false;
        }

        return true;
    }

    public boolean isParametersExactlyExpectedLength(int expectedLength) {
        if (!this.isParametersSet()) {
            return false;
        }

        if (this.parameters.size() != expectedLength) {
            return false;
        }

        return true;
    }

    @Nullable
    public String buildServerOutput() {
        if (!this.isCommandSet()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        // Tags
        if (this.isTagsSet()) {
            // TODO: Implement message tag building
            // http://ircv3.atheme.org/specification/message-tags-3.2
            LOGGER.warn("Message tags to server not implemented yet!");
        }

        // Prefix
        if (this.isPrefixSetAndNotEmpty()) {
            builder.append(CharacterCodes.COLON);
            builder.append(prefix);
            builder.append(CharacterCodes.SPACE);
        }

        // Command
        builder.append(this.command);
        builder.append(CharacterCodes.SPACE);

        // Parameters
        if (this.isParametersAtLeastExpectedLength(1)) {
            builder.append(this.buildParametersString(this.parameters));
        }

        String output = builder.toString();

        int outputLength = output.length();
        if (outputLength < MIN_LENGTH || outputLength > MAX_LENGTH) {
            throw new RuntimeException("Tried to send a message with an invalid length to the server: " + output);
        }

        return output;
    }

    public static String buildParametersString(List<String> parameters) {
        StringBuilder builder = new StringBuilder();

        int parametersSize = parameters.size();
        int lastItem = parametersSize - 1;
        for (int i = 0; i < lastItem; i++) {
            builder.append(parameters.get(i));
            builder.append(CharacterCodes.SPACE);
        }

        builder.append(CharacterCodes.COLON);
        builder.append(parameters.get(lastItem));

        return builder.toString();
    }

    public static class Builder {
        public Map<String, String> tags = null;
        public String prefix = null;
        public String command = null;
        public List<String> parameters = null;

        public Builder command(String command) {
            this.command = command;
            return this;
        }

        public Builder tags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder parameters(String... parameters) {
            return this.parameters(Lists.newArrayList(parameters));
        }

        public Builder parameters(List<String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public IRCMessage build() {
            IRCMessage message = new IRCMessage(this);
            if (message.command == null) {
                throw new IllegalStateException("IRC message must have a command!");
            }

            return message;
        }
    }
}
