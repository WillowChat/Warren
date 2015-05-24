package engineer.carrot.warren.warren;

import engineer.carrot.warren.warren.irc.Channel;

import java.util.List;
import java.util.Set;

public interface IWarrenDelegate {
    public String getBotNickname();

    public ChannelManager getJoiningChannels();

    public ChannelManager getJoinedChannels();

    public UserManager getUserManager();

    public void joinChannels(List<String> channels);

    public void moveJoiningChannelToJoined(String channel);

    public void sendPMToUser(String user, String contents);

    public void sendMessageToChannel(Channel channel, String contents);

    public boolean shouldIdentify();

    public String getIdentifyPassword();

    public List<String> getAutoJoinChannels();

    public void setPrefixes(Set<Character> prefixes);

    public Set<Character> getPrefixes();
}
