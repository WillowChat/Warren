package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import java.util.Set;

public class ChanTypesSupportModule implements IChanTypesSupportModule {
    private final Set<String> channelPrefixes;

    public ChanTypesSupportModule() {
        // TODO: Move default channel types elsewhere (or set defaults from configuration)
        this.channelPrefixes = Sets.newHashSet("#");
    }

    // IISupportModule

    @Override
    public boolean handleValue(String value) {
        // CHANTYPES: &#
        if (Strings.isNullOrEmpty(value)) {
            return false;
        }

        this.channelPrefixes.clear();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            this.channelPrefixes.add(Character.toString(c));
        }

        return true;
    }

    // IChanTypesSupportModule

    @Override
    public Set<String> getChannelPrefixes() {
        return this.channelPrefixes;
    }
}
