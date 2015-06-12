package engineer.carrot.warren.warren.irc.messages.core;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.irc.CharacterCodes;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.IrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

public class ModeMessage extends AbstractMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModeMessage.class);

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
            this.modifiers = this.parseModifiers(message.parameters.subList(1, parametersSize));
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
        public char type;
        public List<String> modes;
        public List<String> parameters;

        public ModeModifier(char type) {
            this.type = type;
            this.modes = Lists.newArrayList();
            this.parameters = Lists.newArrayList();
        }
    }

    private List<ModeModifier> parseModifiers(List<String> modifiers) {
        List<ModeModifier> modeModifiers = Lists.newArrayList();
        // *( ( "-" / "+" ) *<modes> *<modeparams> )
        // Check for a + or a - in the next space

        int modifiersSize = modifiers.size();
        int modifierBaseIndex = 0;

        while (modifierBaseIndex < modifiersSize) {
            String modes = modifiers.get(modifierBaseIndex);
            if (Strings.isNullOrEmpty(modes)) {
                break;
            }

            char type = modes.charAt(0);
            if (!this.isTokenStartOfModifier(type)) {
                LOGGER.warn("Failed to parse MODE modifiers: next token wasn't +-");
                return modeModifiers;
            }

            modifierBaseIndex++;

            ModeModifier modeModifier = new ModeModifier(type);
            modeModifiers.add(modeModifier);

            String restOfModes = modes.substring(1);
            if (!restOfModes.isEmpty()) {
                modeModifier.modes.addAll(this.parseModes(modes.substring(1)));
            }

            // Parse mode parameters

            boolean parsingParameters = true;
            while (parsingParameters) {
                if (modifierBaseIndex > modifiersSize - 1) {
                    // Ran out of tokens
                    parsingParameters = false;
                    continue;
                }

                String nextParameter = modifiers.get(modifierBaseIndex);
                if (Strings.isNullOrEmpty(nextParameter)) {
                    // No more tokens
                    return modeModifiers;
                }

                type = nextParameter.charAt(0);
                if (this.isTokenStartOfModifier(type)) {
                    // New modifier, break out of parameters
                    parsingParameters = false;
                    continue;
                }

                modifierBaseIndex++;
                modeModifier.parameters.add(nextParameter);
            }
        }

        return modeModifiers;
    }

    private boolean isTokenStartOfModifier(char token) {
        return (token == CharacterCodes.PLUS || token == CharacterCodes.MINUS);

    }

    private List<String> parseModes(String token) {
        List<String> modes = Lists.newArrayList();

        for (int i = 0; i < token.length(); i++) {
            modes.add(Character.toString(token.charAt(i)));
        }

        return modes;
    }
}
