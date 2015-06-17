package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

import java.util.Set;

public interface IChanModesSupportModule extends IISupportModule {
    Set<Character> getTypeAModes();

    Set<Character> getTypeBModes();

    Set<Character> getTypeCModes();

    Set<Character> getTypeDModes();
}
