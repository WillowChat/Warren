package engineer.carrot.warren;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.event.ServerConnectedEvent;
import engineer.carrot.warren.event.ServerDisconnectedEvent;
import engineer.carrot.warren.irc.Channel;
import engineer.carrot.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.irc.messages.core.ChangeNicknameMessage;
import engineer.carrot.warren.irc.messages.core.JoinChannelsMessage;
import engineer.carrot.warren.irc.messages.core.PrivMsgMessage;
import engineer.carrot.warren.irc.messages.core.UserMessage;
import engineer.carrot.warren.ssl.WrappedSSLSocketFactory;
import engineer.carrot.warren.util.IMessageQueue;
import engineer.carrot.warren.util.MessageQueue;
import engineer.carrot.warren.util.OutgoingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class IRCServerConnection implements IBotDelegate {
    private final Logger LOGGER = LoggerFactory.getLogger(IRCServerConnection.class);

    private String nickname;
    private String login;
    private String realname;
    private String server;
    private int port;

    private ChannelManager joiningChannelManager;
    private ChannelManager joinedChannelManager;

    private IMessageQueue outgoingQueue;
    private Thread outgoingThread;

    private boolean useFingerprints = false;
    private Set<String> acceptedCertificatesForHost;

    private boolean loginToNickserv = false;
    private String nickservPassword;
    private List<String> autoJoinChannels;

    private EventBus eventBus;

    private IncomingHandler incomingHandler;

    public IRCServerConnection(String server, int port, String nickname) {
        this.server = server;
        this.port = port;
        this.nickname = nickname;
        this.login = this.nickname.substring(0, 1);
        this.realname = this.login;

        this.initialise();
    }

    private void initialise() {
        this.outgoingQueue = new MessageQueue();
        this.eventBus = new EventBus();
        this.incomingHandler = new IncomingHandler(this, this.outgoingQueue, this.eventBus);

        this.joiningChannelManager = new ChannelManager();
        this.joinedChannelManager = new ChannelManager();
    }

    public void registerListener(Object object) {
        this.eventBus.register(object);
    }

    public void setNickservPassword(String password) {
        this.loginToNickserv = true;
        this.nickservPassword = password;
    }

    public void setAutoJoinChannels(List<String> channels) {
        this.autoJoinChannels = channels;
    }

    public void setForciblyAcceptedCertificates(Set<String> certificateFingerprints) {
        this.useFingerprints = true;
        this.acceptedCertificatesForHost = certificateFingerprints;
    }

    public void connect() {
        WrappedSSLSocketFactory ssf = new WrappedSSLSocketFactory();

        if (this.useFingerprints) {
            ssf.forciblyAcceptCertificatesWithSHA1Fingerprints(this.acceptedCertificatesForHost);
        }

        SSLSocket clientSocket;

        try {
            clientSocket = ssf.disableDHEKeyExchange(ssf.createSocket(server, port));
            clientSocket.startHandshake();
            //LOGGER.info(new Gson().toJson(clientSocket.getEnabledCipherSuites()));
        } catch (IOException e) {
            LOGGER.error("Failed to set up socket and start handshake");
            e.printStackTrace();
            return;
        }

        BufferedReader inFromServer;
        OutputStreamWriter outToServer;
        try {
            outToServer = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        this.eventBus.post(new ServerConnectedEvent());

        Runnable outgoingRunnable = new OutgoingRunnable(this.outgoingQueue, outToServer);
        outgoingThread = new Thread(outgoingRunnable);
        outgoingThread.start();

        this.outgoingQueue.addMessageToQueue(new ChangeNicknameMessage(this.nickname));
        this.outgoingQueue.addMessageToQueue(new UserMessage(this.login, "8", this.realname));

        while (true) {
            String serverResponse = null;
            try {
                serverResponse = inFromServer.readLine();
            } catch (IOException e) {
                LOGGER.error("Connection died: {}", e);

                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    LOGGER.error("Failed to close socket: {}", e1);
                }

                this.postDisconnectedEvent();
                this.cleanupOutgoingThread();
                return;
            }

            if (serverResponse == null) {
                LOGGER.error("Server response null");

                this.postDisconnectedEvent();
                this.cleanupOutgoingThread();
                return;
            }

            IRCMessage message = IRCMessage.parseFromLine(serverResponse);

            if (message == null) {
                LOGGER.error("Parsed message was null");

                this.postDisconnectedEvent();
                this.cleanupOutgoingThread();
                return;
            }

            if (message.command == null || message.command.length() < 3) {
                LOGGER.error("Malformed command in message");

                this.postDisconnectedEvent();
                this.cleanupOutgoingThread();
                return;
            }

            boolean failedToHandleMessage = this.incomingHandler.handleIRCMessage(message, serverResponse);
            if (!failedToHandleMessage) {
                LOGGER.error("Failed to handle message. Original: {}", serverResponse);
                return;
            }
        }
    }

    private void postDisconnectedEvent() {
        this.eventBus.post(new ServerDisconnectedEvent());
    }

    private void cleanupOutgoingThread() {
        if (this.outgoingThread == null) {
            return;
        }

        this.outgoingThread.interrupt();

        try {
            this.outgoingThread.join();
        } catch (InterruptedException e) {
            LOGGER.error("Failed to join() on outgoing thread during cleanup: {}", e);
        }

        return;
    }

    // IBotDelegate

    @Override
    public String getBotNickname() {
        return this.nickname;
    }

    @Override
    public ChannelManager getJoiningChannels() {
        return this.joiningChannelManager;
    }

    @Override
    public ChannelManager getJoinedChannels() {
        return this.joinedChannelManager;
    }

    @Override
    public void joinChannels(List<String> channels) {
        for (String channel : channels) {
            this.joiningChannelManager.addChannel(new Channel.Builder().name(channel).users(Maps.newHashMap()).userAccessMap(Maps.newHashMap()).build());
        }

        this.outgoingQueue.addMessageToQueue(new JoinChannelsMessage(channels));
    }

    @Override
    public void moveJoiningChannelToJoined(String channel) {
        if (this.joiningChannelManager.containsChannel(channel)) {
            Channel cChannel = this.joiningChannelManager.getChannel(channel);
            this.joiningChannelManager.removeChannel(channel);

            this.joinedChannelManager.addChannel(cChannel);
            // TODO: fire joined channel event
        }
    }

    @Override
    public void sendPMToUser(String user, String contents) {
        PrivMsgMessage outgoingMessage = new PrivMsgMessage(null, user, contents);
        this.outgoingQueue.addMessageToQueue(outgoingMessage);
    }

    @Override
    public void sendMessageToChannel(Channel channel, String contents) {
        PrivMsgMessage outgoingMessage = new PrivMsgMessage(null, channel.name, contents);
        this.outgoingQueue.addMessageToQueue(outgoingMessage);
    }

    @Override
    public boolean shouldIdentify() {
        return this.loginToNickserv;
    }

    @Override
    public String getIdentifyPassword() {
        return this.nickservPassword;
    }

    @Override
    public List<String> getAutoJoinChannels() {
        return this.autoJoinChannels;
    }

    @Override
    public void setPrefixes(Set<Character> prefixes) {

    }

    @Override
    public Set<Character> getPrefixes() {
        return null;
    }
}
