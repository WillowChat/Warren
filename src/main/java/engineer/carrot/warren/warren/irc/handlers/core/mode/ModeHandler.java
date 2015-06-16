package engineer.carrot.warren.warren.irc.handlers.core.mode;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.handlers.MessageHandler;
import engineer.carrot.warren.warren.irc.messages.core.ModeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModeHandler extends MessageHandler<ModeMessage> {
    private final Logger LOGGER = LoggerFactory.getLogger(ModeHandler.class);

    private final Map<String, IModeHandlerModule> modules;

    public ModeHandler() {
        this.modules = Maps.newHashMap();
    }

    @Override
    public void initialise() {
        this.modules.put("o", new OpModeHandlerModule(this.eventSink, this.botDelegate.getUserManager()));
        this.modules.put("v", new VoiceModeHandlerModule(this.eventSink, this.botDelegate.getUserManager()));
    }

    @Override
    public void handleMessage(ModeMessage message) {
        Set<String> channelPrefixes = this.incomingHandler.getISupportManager().getChannelPrefixesModule().getChannelPrefixes();
        String prefix = message.target.substring(0, 1);
        if (channelPrefixes.contains(prefix)) {
            // Channel MODE

            Channel channel = this.botDelegate.getJoinedChannels().getChannel(message.target);
            if (channel == null) {
                LOGGER.info("Got a channel MODE for a channel we don't think we're in:");
                LOGGER.info("{}", new Gson().toJson(message));

                return;
            }

            List<ModeMessage.ModeModifier> modifiers = message.modifiers;
            List<String> unhandledModes = Lists.newArrayList();
            for (ModeMessage.ModeModifier modifier : modifiers) {
                IModeHandlerModule module = this.modules.get(modifier.mode);
                if (module == null) {
                    unhandledModes.add(modifier.mode);
                    continue;
                }

                module.handleModeChange(modifier, channel);
            }

            if (!unhandledModes.isEmpty()) {
                LOGGER.warn("Didn't know how to handle the following MODE changes: {}", Joiner.on(", ").join(unhandledModes));
            }

            return;
        }

        // User MODE

        LOGGER.info("Handling user MODE:");
        LOGGER.info("{}", new Gson().toJson(message));
    }
}