package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

import java.util.Set;

public interface IChanModesSupportModule extends IISupportModule {
    Set<String> getTypeAModes();

    Set<String> getTypeBModes();

    Set<String> getTypeCModes();

    Set<String> getTypeDModes();
}
