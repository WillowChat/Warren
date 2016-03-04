package engineer.carrot.warren.warren.irc.messages;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import engineer.carrot.warren.warren.irc.JavaCharacterCodes;
import engineer.carrot.warren.warren.irc.Hostmask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class JavaIrcMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaIrcMessage.class);

    private final Map<String, String> tags;
    public final String prefix;
    public final List<String> parameters;
    public final String command;

    public static final int MAX_LENGTH = 510;
    public static final int MIN_LENGTH = 3;

    private JavaIrcMessage(Builder builder) {
        this.tags = builder.tags;
        this.prefix = builder.prefix;
        this.command = builder.command;
        this.parameters = builder.parameters;
    }

    public String buildPrettyOutput() {
        return new Gson().toJson(this);
    }

    @Nullable
    public static JavaIrcMessage parseFromLine(String line) {
        int length = line.length();
        if (length > MAX_LENGTH || length < MIN_LENGTH) {
            return null;
        }

        Builder builder = new Builder();

        int stringPosition = 0;

        // http://ircv3.atheme.org/specification/message-tags-3.2

        // Tags
        if (line.charAt(stringPosition) == JavaCharacterCodes.AT) {
            int nextSpace = line.indexOf(JavaCharacterCodes.SPACE, stringPosition);
            if (nextSpace < 0) {
                LOGGER.error("Malformed tag: no trailing space found");
                return null;
            }

            Map<String, String> tags = Maps.newHashMap();

            String[] tagsArray = line.substring(1, nextSpace).split(String.valueOf(JavaCharacterCodes.SEMICOLON));
            for (String tag : tagsArray) {
                String[] splitTag = tag.split(String.valueOf(JavaCharacterCodes.EQUALS), 2);
                if (splitTag.length < 2) {
                    LOGGER.error("Malformed tag: no key or value");
                    return null;
                }

                tags.put(splitTag[0], splitTag[1]);
            }

            builder.tags(tags);

            stringPosition = nextSpace + 1;

            // Trim trailing spaces (some IRC daemons had this)
            while (stringPosition < length && line.charAt(stringPosition) == JavaCharacterCodes.SPACE) {
                stringPosition++;
            }
        }

        if (stringPosition >= length) {
            LOGGER.error("Malformed message: tags but nothing else");
            return null;
        }

        // Prefix
        if (line.charAt(stringPosition) == JavaCharacterCodes.COLON) {
            int nextSpace = line.indexOf(JavaCharacterCodes.SPACE, stringPosition);
            if (nextSpace < 0) {
                LOGGER.error("Malformed prefix: no trailing space found");
                return null;
            }

            builder.prefix(line.substring(stringPosition + 1, nextSpace));

            stringPosition = nextSpace + 1;

            // Trim trailing spaces (some IRC daemons had this)
            while (stringPosition < length && line.charAt(stringPosition) == JavaCharacterCodes.SPACE) {
                stringPosition++;
            }
        }

        if (stringPosition >= length) {
            LOGGER.error("Malformed message: prefix but nothing else");
            return null;
        }

        // Command
        int nextSpace = line.indexOf(JavaCharacterCodes.SPACE, stringPosition);
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
        while (stringPosition < length && line.charAt(stringPosition) == JavaCharacterCodes.SPACE) {
            stringPosition++;
        }

        // Parameters

        List<String> parameters = Lists.newArrayList();

        while (stringPosition < length) {
            if (line.charAt(stringPosition) == JavaCharacterCodes.COLON) {
                // Rest of the message is one parameter

                if ((stringPosition + 1) >= length) {
                    LOGGER.error("Malformed message: ends in a colon indicating a message, but with nothing else");
                    return null;
                }

                String finalParam = line.substring(stringPosition + 1);
                parameters.add(finalParam);

                return builder.parameters(parameters).build();
            }

            nextSpace = line.indexOf(JavaCharacterCodes.SPACE, stringPosition);
            if (nextSpace >= 0) {
                String param = line.substring(stringPosition, nextSpace);
                parameters.add(param);
                stringPosition = nextSpace + 1;

                // Trim trailing spaces (some IRC daemons had this)
                while (stringPosition < length && line.charAt(stringPosition) == JavaCharacterCodes.SPACE) {
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

    public boolean hasPrefix() {
        return !this.prefix.isEmpty();
    }

    public boolean hasTags() {
        return !this.tags.isEmpty();
    }

    public boolean hasParameters() {
        return this.parameters != null && !this.parameters.isEmpty();
    }

    @Nullable
    public String buildServerOutput() {
        StringBuilder builder = new StringBuilder();

        // Tags
        if (this.hasTags()) {
            // TODO: Implement message tag building
            // http://ircv3.atheme.org/specification/message-tags-3.2
            LOGGER.warn("Message tags to server not implemented yet!");
        }

        // Prefix
        if (this.hasPrefix()) {
            builder.append(JavaCharacterCodes.COLON);
            builder.append(prefix);
            builder.append(JavaCharacterCodes.SPACE);
        }

        // Command
        builder.append(this.command);
        builder.append(JavaCharacterCodes.SPACE);

        // Parameters
        if (this.parameters.size() >= 1) {
            builder.append(buildParametersString(this.parameters));
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
            builder.append(JavaCharacterCodes.SPACE);
        }

        builder.append(JavaCharacterCodes.COLON);
        builder.append(parameters.get(lastItem));

        return builder.toString();
    }

    public static class Builder {
        public Map<String, String> tags = Maps.newHashMap();
        public String prefix = "";
        public String command = "";
        public List<String> parameters = Lists.newArrayList();

        public Builder command(String command) {
            this.command = command;
            return this;
        }

        public Builder tags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder prefix(Hostmask hostmask) {
            this.prefix = hostmask.buildOutputString();
            return this;
        }

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder parameter(String parameter) {
            this.parameters.add(parameter);
            return this;
        }

        public Builder parameters(String... parameters) {
            return this.parameters(Lists.newArrayList(parameters));
        }

        public Builder parameters(List<String> parameters) {
            this.parameters.addAll(parameters);
            return this;
        }

        public JavaIrcMessage build() {
            if (this.command.isEmpty()) {
                throw new IllegalStateException("IRC message must have a command!");
            }

            return new JavaIrcMessage(this);
        }
    }
}
