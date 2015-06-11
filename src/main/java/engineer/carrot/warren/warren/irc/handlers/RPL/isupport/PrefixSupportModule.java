package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import engineer.carrot.warren.warren.irc.CharacterCodes;

import java.util.Map;

public class PrefixSupportModule implements IISupportModule {
    private final Map<String, String> modesToPrefixes;
    private final Map<String, String> prefixesToModes;

    public PrefixSupportModule() {
        this.modesToPrefixes = Maps.newHashMap();
        this.prefixesToModes = Maps.newHashMap();
    }

    @Override
    public boolean handleValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return false;
        }

        if (value.charAt(0) != CharacterCodes.LEFT_BRACKET) {
            return false;
        }

        value = value.substring(1);

        int rightBracketPosition = value.indexOf(CharacterCodes.RIGHT_BRACKET);
        if (rightBracketPosition <= 1) {
            return false;
        }

        if (value.endsWith(Character.toString(CharacterCodes.RIGHT_BRACKET))) {
            return false;
        }

        String modes = value.substring(0, rightBracketPosition);
        String prefixes = value.substring(rightBracketPosition + 1);

        int modesLength = modes.length();
        int prefixesLength = prefixes.length();

        if (modesLength == 0 || prefixesLength == 0 || (modesLength != prefixesLength)) {
            return false;
        }

        // PREFIX: (ov)@+

        for (int i = 0; i < modesLength; i++) {
            String mode = Character.toString(modes.charAt(i));
            String prefix = Character.toString(prefixes.charAt(i));

            if (Strings.isNullOrEmpty(mode) || Strings.isNullOrEmpty(prefix)) {
                return false;
            }

            modesToPrefixes.put(mode, prefix);
            prefixesToModes.put(prefix, mode);
        }

        return true;
    }
}
