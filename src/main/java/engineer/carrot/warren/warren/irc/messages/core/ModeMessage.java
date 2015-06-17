package engineer.carrot.warren.warren.irc.messages.core;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.irc.CharacterCodes;
import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IChanModesSupportModule;
import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IPrefixSupportModule;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ModeMessage extends AbstractMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModeMessage.class);

    private IChanModesSupportModule chanModesSupportModule;
    private IPrefixSupportModule prefixSupportModule;

    @Nullable
    public String fromUser;
    public String target;
    public List<ModeModifier> modifiers;

    public ModeMessage() {

    }

    @Override
    public void populateFromIRCMessage(IrcMessage message) {
        this.fromUser = message.prefix;
        this.target = message.parameters.get(0);

        int parametersSize = message.parameters.size();
        if (parametersSize > 1) {
            this.modifiers = this.parseIrcParameters(message.parameters.subList(1, parametersSize));
        }
    }

    @Override
    public boolean isMessageWellFormed(IrcMessage message) {
        // // <channel> *( ( "-" / "+" ) *<modes> *<modeparams> )
        return message.isParametersAtLeastExpectedLength(1);
    }

    @Override
    public String getCommandID() {
        return MessageCodes.MODE;
    }

    // Mode parsing

    public class ModeModifier {
        public final char type;
        public final Character mode;
        public String parameter;
        public String setter;

        public ModeModifier(char type, Character mode, String setter) {
            this.type = type;
            this.mode = mode;

            if (setter == null) {
                this.setter = "";
            } else {
                this.setter = setter;
            }

            this.parameter = "";
        }

        public boolean hasParameter() {
            return !Strings.isNullOrEmpty(this.parameter);
        }

        public boolean isAdding() {
            return (this.type == CharacterCodes.PLUS);
        }

        public boolean isRemoving() {
            return (this.type == CharacterCodes.MINUS);
        }
    }

    public class ModeChunk {
        public final String modes;
        public final Queue<String> parameters;

        public ModeChunk(String modes) {
            this.modes = modes;
            this.parameters = new LinkedList<>();
        }
    }

    private List<ModeModifier> parseIrcParameters(List<String> parameters) {
        // *( ( "-" / "+" ) *<modes> *<modeparams> )

        List<ModeChunk> chunks = this.parseParametersToModeChunks(parameters);
        return this.parseChunksToModifiers(chunks);
    }

    private boolean isTokenStartOfModifier(Character token) {
        return (token == CharacterCodes.PLUS || token == CharacterCodes.MINUS);

    }

    private List<ModeChunk> parseParametersToModeChunks(List<String> parameters) {
        List<ModeChunk> chunks = Lists.newArrayList();

        if (parameters == null || parameters.isEmpty()) {
            return chunks;
        }

        ModeChunk currentChunk = null;

        for (String parameter : parameters) {
            if (parameter.isEmpty()) {
                LOGGER.warn("Attempted to parse an empty parameter in to a chunk - bailing");

                break;
            }

            if (this.isTokenStartOfModifier(parameter.charAt(0))) {
                currentChunk = new ModeChunk(parameter);
                chunks.add(currentChunk);

                continue;
            }

            if (currentChunk == null) {
                LOGGER.warn("Attempted to add a chunk without having a type token first - bailing");

                break;
            }

            currentChunk.parameters.add(parameter);
        }

        return chunks;
    }

    private List<ModeModifier> parseChunksToModifiers(List<ModeChunk> chunks) {
        List<ModeModifier> modifiers = Lists.newArrayList();

        Character currentMode = 0;

        for (ModeChunk chunk : chunks) {
            for (Character token : this.parseModes(chunk.modes)) {
                if (this.isTokenStartOfModifier(token)) {
                    currentMode = token;

                    continue;
                }

                if (currentMode == 0) {
                    LOGGER.warn("Tried to add a modifier that didn't start with +- - bailing: '{}'", token);

                    continue;
                }

                ModeModifier modifier = new ModeModifier(currentMode, token, this.fromUser);

                boolean isAdding = (currentMode == CharacterCodes.PLUS);
                boolean takesAParameter = this.takesAParameter(isAdding, token);

                if (takesAParameter) {
                    String parameter = chunk.parameters.poll();

                    if (Strings.isNullOrEmpty(parameter)) {
                        LOGGER.warn("MODE modifier was missing an expected parameter - not processing it: '{}'", token);

                        continue;
                    }

                    modifier.parameter = parameter;
                }

                modifiers.add(modifier);
            }

            if (!chunk.parameters.isEmpty()) {
                LOGGER.warn("Chunk had parameters left after polling - something probably went wrong!");
            }
        }

        return modifiers;
    }

    private List<Character> parseModes(String token) {
        List<Character> modes = Lists.newArrayList();

        for (int i = 0; i < token.length(); i++) {
            modes.add(token.charAt(i));
        }

        return modes;
    }

    private boolean takesAParameter(boolean isAdding, Character mode) {
        /*
        Type A: always takes a parameter
        Type B: always takes a parameter
        Type C: takes a parameter only in + mode
        Type D: never takes a parameter

        Prefixes can be assumed to be Type B (always takes a parameter)
         */

        IChanModesSupportModule chanmodes = this.iSupportManager.getChannelModesModule();
        IPrefixSupportModule prefixes = this.iSupportManager.getPrefixModule();

        if (prefixes.getModes().contains(mode)) {
            return true;
        }

        if (chanmodes.getTypeDModes().contains(mode)) {
            return false;
        }

        return (chanmodes.getTypeAModes().contains(mode)
                || chanmodes.getTypeBModes().contains(mode)
                || (!isAdding && chanmodes.getTypeCModes().contains(mode)));
    }
}
