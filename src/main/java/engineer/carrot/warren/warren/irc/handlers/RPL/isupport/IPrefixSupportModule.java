package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

import javax.annotation.Nullable;
import java.util.Set;

public interface IPrefixSupportModule extends IISupportModule {
    Set<Character> getPrefixes();

    Set<Character> getModes();

    @Nullable
    Character getModeFromPrefix(Character prefix);
}
