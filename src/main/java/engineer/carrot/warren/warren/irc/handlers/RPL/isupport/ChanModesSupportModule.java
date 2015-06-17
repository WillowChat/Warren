package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class ChanModesSupportModule implements IChanModesSupportModule {
    private final Logger LOGGER = LoggerFactory.getLogger(ChanModesSupportModule.class);

    private final Set<Character> typeAModes;
    private final Set<Character> typeBModes;
    private final Set<Character> typeCModes;
    private final Set<Character> typeDModes;

    public ChanModesSupportModule() {
        this.typeAModes = Sets.newHashSet();
        this.typeBModes = Sets.newHashSet();
        this.typeCModes = Sets.newHashSet();
        this.typeDModes = Sets.newHashSet();
    }

    // IISupportModule

    @Override
    public boolean handleValue(String value) {
        // CHANMODES: eIb,k,l,imnpstSr

        if (Strings.isNullOrEmpty(value)) {
            return false;
        }

        List<String> modeValues = Splitter.on(",").splitToList(value);
        if (modeValues.size() < 4) {
            LOGGER.warn("Server returned fewer channel modes than Warren supports - needs at least 4. Not attempting to parse");

            return false;
        }

        this.typeAModes.clear();
        this.typeBModes.clear();
        this.typeCModes.clear();
        this.typeDModes.clear();

        this.typeAModes.addAll(this.parseModes(modeValues.get(0)));
        this.typeBModes.addAll(this.parseModes(modeValues.get(1)));
        this.typeCModes.addAll(this.parseModes(modeValues.get(2)));
        this.typeDModes.addAll(this.parseModes(modeValues.get(3)));

        return true;
    }

    private Set<Character> parseModes(String typeValues) {
        Set<Character> parsedModes = Sets.newHashSet();

        for (int i = 0; i < typeValues.length(); i++) {
            char c = typeValues.charAt(i);

            parsedModes.add(c);
        }

        return parsedModes;
    }

    // IChanModesSupportModule

    @Override
    public Set<Character> getTypeAModes() {
        return this.typeAModes;
    }

    @Override
    public Set<Character> getTypeBModes() {
        return this.typeBModes;
    }

    @Override
    public Set<Character> getTypeCModes() {
        return this.typeCModes;
    }

    @Override
    public Set<Character> getTypeDModes() {
        return typeDModes;
    }
}
