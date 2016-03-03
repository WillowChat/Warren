package engineer.carrot.warren.warren.irc.messages.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import engineer.carrot.warren.warren.irc.messages.AbstractMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import engineer.carrot.warren.warren.irc.messages.MessageCodes;

import java.util.List;
import java.util.Map;

public class JoinChannelsMessage extends AbstractMessage {
    private final List<String> channels;
    private final List<String> keys = Lists.newArrayList();

    public JoinChannelsMessage(String channel) {
        this.channels = Lists.newArrayList(channel);
    }

    public JoinChannelsMessage(String channel, String key) {
        this.channels = Lists.newArrayList(channel);
        keys.add(key);
    }

    public JoinChannelsMessage(List<String> channels) {
        this.channels = channels;
    }

    public JoinChannelsMessage(Map<String, String> channelsAndKeys) {
        this.channels = Lists.newArrayList();

        for (Map.Entry<String, String> entry : channelsAndKeys.entrySet()) {
            String channelName = entry.getKey();
            String channelKey = entry.getValue();

            this.channels.add(channelName);
            this.keys.add(channelKey);
        }
    }

    // Outbound

    @Override
    public JavaIrcMessage build() {
        if (this.keys.isEmpty()) {
            return new JavaIrcMessage.Builder()
                    .command(this.getCommand())
                    .parameters(Joiner.on(",").join(this.channels))
                    .build();
        } else {
            return new JavaIrcMessage.Builder()
                    .command(this.getCommand())
                    .parameters(Joiner.on(",").join(this.channels), Joiner.on(",").join(this.keys))
                    .build();
        }
    }

    // Shared

    @Override
    public String getCommand() {
        return MessageCodes.JOIN;
    }
}
