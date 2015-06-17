package engineer.carrot.warren.warren.irc.handlers.core.mode;

import engineer.carrot.warren.warren.IEventSink;
import engineer.carrot.warren.warren.UserManager;
import engineer.carrot.warren.warren.event.mode.ChannelDevoiceEvent;
import engineer.carrot.warren.warren.event.mode.ChannelVoiceEvent;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.Hostmask;
import engineer.carrot.warren.warren.irc.User;
import engineer.carrot.warren.warren.irc.messages.core.ModeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceModeHandlerModule extends ModeHandlerModule {
    private final Logger LOGGER = LoggerFactory.getLogger(VoiceModeHandlerModule.class);

    public VoiceModeHandlerModule(IEventSink eventSink, UserManager userManager) {
        super(eventSink, userManager);
    }

    @Override
    public boolean handleModeChange(ModeMessage.ModeModifier modifier, Channel channel) {
        if (!modifier.hasParameter()) {
            LOGGER.warn("User VOICE status changed but a parameter (who) wasn't set");

            return false;
        }

        User receivingUser = channel.getOrCreateUser(Hostmask.parseFromString(modifier.parameter), this.userManager);
        User settingUser = this.userManager.getOrCreateUser(Hostmask.parseFromString(modifier.setter));

        if (modifier.isAdding()) {
            channel.addUserMode(receivingUser, modifier.mode);

            this.eventSink.postEvent(new ChannelVoiceEvent(settingUser, receivingUser, channel));
        } else {
            channel.removeUserMode(receivingUser, modifier.mode);

            this.eventSink.postEvent(new ChannelDevoiceEvent(settingUser, receivingUser, channel));
        }

        return true;
    }
}
