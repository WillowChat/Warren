package engineer.carrot.warren.warren.irc.handlers.RPL.isupport;

public interface IISupportManager {
    IPrefixSupportModule getPrefixModule();

    IChanTypesSupportModule getChannelPrefixesModule();

    IChanModesSupportModule getChannelModesModule();
}
