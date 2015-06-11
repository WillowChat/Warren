package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

import java.util.Set;

public interface IPrefixSupportModule extends IISupportModule {
    Set<String> getPrefixes();
}
