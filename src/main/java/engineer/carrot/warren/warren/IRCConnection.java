package engineer.carrot.warren.warren;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import engineer.carrot.warren.warren.event.ServerConnectedEvent;
import engineer.carrot.warren.warren.event.ServerDisconnectedEvent;
import engineer.carrot.warren.warren.irc.AccessLevel;
import engineer.carrot.warren.warren.irc.Channel;
import engineer.carrot.warren.warren.irc.User;
import engineer.carrot.warren.warren.irc.messages.IRCMessage;
import engineer.carrot.warren.warren.irc.messages.core.*;
import engineer.carrot.warren.warren.ssl.WrappedSSLSocketFactory;
import engineer.carrot.warren.warren.util.IMessageQueue;
import engineer.carrot.warren.warren.util.MessageQueue;
import engineer.carrot.warren.warren.util.OutgoingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class IRCConnection implements IWarrenDelegate {
    private final Logger LOGGER = LoggerFactory.getLogger(IRCConnection.class);
    private static final long SOCKET_TIMEOUT_NS = 60 * 1000000000L;
    private static final int SOCKET_INTERRUPT_TIMEOUT_MS = 1 * 1000;

    private final String nickname;
    private final String login;
    private final String realname;
    private final String server;
    private final int port;
    private boolean loginToNickserv = false;
    private String nickservPassword;
    private List<String> autoJoinChannels;
    private boolean plaintext = false;
    private boolean useFingerprints = false;
    private Set<String> fingerprints;

    private ChannelManager joiningChannelManager;
    private ChannelManager joinedChannelManager;

    private UserManager userManager;

    private IMessageQueue outgoingQueue;
    private Thread outgoingThread;

    private EventBus eventBus;

    private IncomingHandler incomingHandler;
    private BufferedReader currentReader;

    private IRCConnection(Builder builder) {
        this.server = builder.server;
        this.port = builder.port;
        this.nickname = builder.nickname;
        this.login = builder.login;
        this.realname = builder.login;

        this.initialise();

        this.setSocketShouldUsePlaintext(builder.plaintext);
        this.setAcceptedFingerprints(builder.fingerprints);

        this.setNickservPassword(builder.nickservPassword);
        this.setAutoJoinChannels(builder.channels);

        this.registerListeners(builder.listeners);
    }

    // Initialisation

    private void initialise() {
        this.outgoingQueue = new MessageQueue();
        this.eventBus = new EventBus();
        this.userManager = new UserManager(Sets.<String>newHashSet());

        this.incomingHandler = new IncomingHandler(this, this.outgoingQueue, this.eventBus);

        this.joiningChannelManager = new ChannelManager();
        this.joinedChannelManager = new ChannelManager();
    }

    private void setSocketShouldUsePlaintext(boolean shouldUsePlaintext) {
        this.plaintext = shouldUsePlaintext;
    }

    private void setAcceptedFingerprints(Set<String> fingerprints) {
        this.useFingerprints = !fingerprints.isEmpty();
        this.fingerprints = fingerprints;
    }

    private void setNickservPassword(String password) {
        this.loginToNickserv = !Strings.isNullOrEmpty(password);
        this.nickservPassword = password;
    }

    private void setAutoJoinChannels(List<String> channels) {
        this.autoJoinChannels = channels;
    }

    private void registerListeners(List<Object> objects) {
        for (Object object : objects) {
            this.eventBus.register(object);
        }
    }

    // Public lifecycle

    public void connect() {
        Socket clientSocket;
        if (this.plaintext) {
            clientSocket = this.createPlaintextSocket(this.server, this.port);
        } else {
            clientSocket = this.createTLSSocket(this.server, this.port, this.useFingerprints, this.fingerprints);
        }

        if (clientSocket == null) {
            LOGGER.error("Failed to create socket for connection");
            return;
        }

        OutputStreamWriter outToServer;
        try {
            outToServer = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8);
            this.currentReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        this.eventBus.post(new ServerConnectedEvent());

        Runnable outgoingRunnable = new OutgoingRunnable(this.outgoingQueue, outToServer);
        this.outgoingThread = new Thread(outgoingRunnable);
        this.outgoingThread.start();

        this.outgoingQueue.addMessageToQueue(new ChangeNicknameMessage(this.nickname));
        this.outgoingQueue.addMessageToQueue(new UserMessage(this.login, "8", this.realname));

        long lastResponseTime = System.nanoTime();

        while (!Thread.currentThread().isInterrupted()) {
            String serverResponse;

            try {
                serverResponse = this.currentReader.readLine();
            } catch (SocketTimeoutException e) {
                if ((System.nanoTime() - lastResponseTime) > SOCKET_TIMEOUT_NS) {
                    // Socket read timed out - try to write a PING and read again

                    this.outgoingQueue.addMessageToQueue(new PingMessage("idle"));
                    lastResponseTime = System.nanoTime();
                }
                continue;
            } catch (IOException e) {
                LOGGER.error("Connection died: {}", e);

                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    LOGGER.error("Failed to close socket: {}", e1);
                }

                break;
            }

            if (serverResponse == null) {
                LOGGER.error("Server response null");

                break;
            }

            lastResponseTime = System.nanoTime();
            IRCMessage message = IRCMessage.parseFromLine(serverResponse);

            if (message == null) {
                LOGGER.error("Parsed message was null");

                break;
            }

            if (message.command == null || message.command.length() < 3) {
                LOGGER.error("Malformed command in message");

                break;
            }

            boolean handledMessage = this.incomingHandler.handleIRCMessage(message, serverResponse);
            if (!handledMessage) {
                LOGGER.error("Failed to handle message. Original: {}", serverResponse);

                break;
            }
        }

        this.disconnect();
        this.postDisconnectedEvent();
        this.cleanupOutgoingThread();
    }

    private boolean disconnect() {
        if (this.currentReader == null) {
            return false;
        }

        try {
            this.currentReader.close();
        } catch (IOException e) {
        }

        return true;
    }

    // Private helpers

    @Nullable
    private Socket createPlaintextSocket(String server, int port) {
        Socket socket;
        try {
            socket = new Socket(server, port);
            socket.setSoTimeout(SOCKET_INTERRUPT_TIMEOUT_MS); // Read once a second for interrupts
        } catch (IOException e) {
            LOGGER.error("Failed to set up plaintext socket");
            e.printStackTrace();
            return null;
        }

        return socket;
    }

    @Nullable
    private Socket createTLSSocket(String server, int port, boolean useFingerprints, Set<String> fingerprints) {
        Socket socket;
        WrappedSSLSocketFactory ssf = new WrappedSSLSocketFactory();

        if (useFingerprints) {
            ssf.forciblyAcceptCertificatesWithSHA1Fingerprints(fingerprints);
        }

        try {
            socket = ssf.disableDHEKeyExchange(ssf.createSocket(server, port));
            socket.setSoTimeout(SOCKET_INTERRUPT_TIMEOUT_MS); // Read once a second for interrupts
            ((SSLSocket) socket).startHandshake();
            //LOGGER.info(new Gson().toJson(clientSocket.getEnabledCipherSuites()));
        } catch (IOException e) {
            LOGGER.error("Failed to set up socket and start handshake");
            e.printStackTrace();
            return null;
        }

        return socket;
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

        }

        return;
    }

    public static class Builder {
        public String server = "";
        public int port = 6667;
        public String nickname = "";
        public String login = "";
        public final List<Object> listeners = Lists.newArrayList();
        public String nickservPassword = "";
        public final List<String> channels = Lists.newArrayList();
        public boolean plaintext = false;
        public final Set<String> fingerprints = Sets.newHashSet();

        public Builder server(String server) {
            this.server = server;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder login(String login) {
            this.login = login;
            return this;
        }

        public Builder listener(Object listener) {
            this.listeners.add(listener);
            return this;
        }

        public Builder listeners(List<Object> listeners) {
            this.listeners.addAll(listeners);
            return this;
        }

        public Builder nickservPassword(String password) {
            this.nickservPassword = password;
            return this;
        }

        public Builder channel(String channel) {
            this.channels.add(channel);
            return this;
        }

        public Builder channels(List<String> channels) {
            this.channels.addAll(channels);
            return this;
        }

        public Builder plaintext(boolean plaintext) {
            this.plaintext = plaintext;
            return this;
        }

        public Builder fingerprint(String fingerprint) {
            this.fingerprints.add(fingerprint);
            return this;
        }

        public Builder fingerprints(Set<String> fingerprints) {
            this.fingerprints.addAll(fingerprints);
            return this;
        }

        public IRCConnection build() {
            // TODO: Parameter verification

            return new IRCConnection(this);
        }
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
    public UserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public void joinChannels(List<String> channels) {
        for (String channel : channels) {
            this.joiningChannelManager.addChannel(new Channel.Builder().name(channel).users(Sets.<User>newHashSet()).userAccessMap(Maps.<User, AccessLevel>newHashMap()).build());
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
