package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import engineer.carrot.warren.warren.IPrefixListener;
import engineer.carrot.warren.warren.irc.JavaCharacterCodes;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrefixSupportModule implements IPrefixSupportModule {
    private final Set<Character> prefixes;
    private final Set<Character> modes;
    private final Map<Character, Character> modesToPrefixes;
    private final Map<Character, Character> prefixesToModes;
    private final Map<Character, Integer> modesToIndexes;

    private final List<IPrefixListener> listeners;

    public PrefixSupportModule(List<IPrefixListener> listeners) {
        // TODO: Add default (ov)@+
        // TODO: Handle "null" value
        this.prefixes = Sets.newHashSet();
        this.modes = Sets.newHashSet();
        this.modesToPrefixes = Maps.newHashMap();
        this.prefixesToModes = Maps.newHashMap();
        this.modesToIndexes = Maps.newHashMap();

        this.listeners = listeners;
    }

    @Override
    public boolean handleValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return false;
        }

        if (value.charAt(0) != JavaCharacterCodes.LEFT_BRACKET) {
            return false;
        }

        value = value.substring(1);

        int rightBracketPosition = value.indexOf(JavaCharacterCodes.RIGHT_BRACKET);
        if (rightBracketPosition <= 1) {
            return false;
        }

        if (value.endsWith(Character.toString(JavaCharacterCodes.RIGHT_BRACKET))) {
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
            Character mode = modes.charAt(i);
            Character prefix = prefixes.charAt(i);

            this.prefixes.add(prefix);
            this.modes.add(mode);
            this.modesToPrefixes.put(mode, prefix);
            this.prefixesToModes.put(prefix, mode);
            this.modesToIndexes.put(mode, i);
        }

        for (IPrefixListener listener : this.listeners) {
            listener.prefixesChanged(this.prefixes);
        }

        return true;
    }

    // IPrefixSupportModule

    @Override
    public Set<Character> getPrefixes() {
        return this.prefixes;
    }

    @Override
    public Set<Character> getModes() {
        return this.modes;
    }

    @Override
    public Character getModeFromPrefix(Character prefix) {
        return this.prefixesToModes.get(prefix);
    }

    @Override
    public int getModePosition(Character mode) {
        Integer modeIndex = this.modesToIndexes.get(mode);
        if (modeIndex == null || modeIndex < 0) {
            return Integer.MAX_VALUE;
        }

        return modeIndex;
    }
}
