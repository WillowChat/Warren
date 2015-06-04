package engineer.carrot.warren.warren;

import engineer.carrot.warren.warren.irc.Channel;

import java.util.List;
import java.util.Set;

public interface IWarrenDelegate {
    String getBotNickname();

    ChannelManager getJoiningChannels();

    ChannelManager getJoinedChannels();

    UserManager getUserManager();

    void joinChannels(List<String> channels);

    void moveJoiningChannelToJoined(String channel);

    void sendPMToUser(String user, String contents);

    void sendMessageToChannel(Channel channel, String contents);

    boolean shouldIdentify();

    String getIdentifyPassword();

    List<String> getAutoJoinChannels();

    void setPrefixes(Set<Character> prefixes);

    Set<Character> getPrefixes();
}
