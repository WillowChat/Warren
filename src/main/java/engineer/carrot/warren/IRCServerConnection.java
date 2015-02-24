package engineer.carrot.warren;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import engineer.carrot.warren.event.ServerConnectedEvent;
import engineer.carrot.warren.irc.handlers.RPL.*;
import engineer.carrot.warren.irc.handlers.core.JoinHandler;
import engineer.carrot.warren.irc.handlers.core.PartHandler;
import engineer.carrot.warren.irc.messages.IMessage;
import engineer.carrot.warren.irc.messages.RPL.*;
import engineer.carrot.warren.irc.messages.core.*;
import engineer.carrot.warren.event.ServerDisconnectedEvent;
import engineer.carrot.warren.irc.handlers.IMessageHandler;
import engineer.carrot.warren.irc.handlers.core.PingHandler;
import engineer.carrot.warren.irc.handlers.core.PrivMsgHandler;
import engineer.carrot.warren.ssl.WrappedSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IRCServerConnection implements IBotDelegate {
    final Logger LOGGER = LoggerFactory.getLogger(IRCServerConnection.class);
    Gson messageGson;

    String nickname;
    String login;
    String realname;
    String server;
    int port;

    Map<String, IMessageHandler> commandDelegateMap;
    Map<String, Class<? extends IMessage>> messageMap;

    ChannelManager joiningChannelManager;
    ChannelManager joinedChannelManager;

    IMessageQueue outgoingQueue;
    Thread outgoingThread;

    boolean useFingerprints = false;
    Set<String> acceptedCertificatesForHost;

    boolean loginToNickserv = false;
    String nickservPassword;
    List<String> autoJoinChannels;

    EventBus eventBus;

    public IRCServerConnection(EventBus eventBus, String server, int port, String nickname) {
        this.eventBus = eventBus;
        this.server = server;
        this.port = port;
        this.nickname = nickname;
        this.login = this.nickname.substring(0, 1);
        this.realname = this.login;

        this.initialise();
    }

    public void initialise() {
        this.outgoingQueue = new MessageQueue();

        this.messageGson = new Gson();

        this.commandDelegateMap = Maps.newHashMap();
        this.messageMap = Maps.newHashMap();

        // TODO: Find this automatically with annotations or something

        this.addMessageToMap(new CreatedMessage());
        this.addMessageToMap(new MOTDMessage());
        this.addMessageToMap(new MOTDStartMessage());
        this.addMessageToMap(new NoticeMessage());
        this.addMessageToMap(new PongMessage());
        this.addMessageToMap(new TopicWhoTimeMessage());
        this.addMessageToMap(new YourHostMessage());

        this.addMessageHandlerPairToMap(new EndOfMOTDMessage(), new EndOfMOTDHandler());
        this.addMessageHandlerPairToMap(new NoTopicMessage(), new NoTopicHandler());
        this.addMessageHandlerPairToMap(new TopicMessage(), new TopicHandler());
        this.addMessageHandlerPairToMap(new WelcomeMessage(), new WelcomeHandler());
        this.addMessageHandlerPairToMap(new JoinedChannelMessage(), new JoinHandler());
        this.addMessageHandlerPairToMap(new PartChannelMessage(), new PartHandler());
        this.addMessageHandlerPairToMap(new PingMessage(), new PingHandler());
        this.addMessageHandlerPairToMap(new PrivMsgMessage(), new PrivMsgHandler());
        this.addMessageHandlerPairToMap(new NamReplyMessage(), new NamReplyHandler());
        this.addMessageHandlerPairToMap(new ISupportMessage(), new ISupportHandler());


        for (IMessageHandler handler : this.commandDelegateMap.values()) {
            handler.setBotDelegate(this);
            handler.setOutgoingQueue(this.outgoingQueue);
            handler.setEventBus(this.eventBus);
        }

        this.joiningChannelManager = new ChannelManager();
        this.joinedChannelManager = new ChannelManager();
    }

    public void addMessageToMap(IMessage message) {
        this.messageMap.put(message.getCommandID(), message.getClass());
    }

    public void addMessageHandlerPairToMap(IMessage message, IMessageHandler handler) {
        if (this.messageMap.containsKey(message.getCommandID())) {
            throw new RuntimeException("Cannot add a message handler pair when said message is already in the map: " + message.getCommandID());
        }

        this.messageMap.put(message.getCommandID(), message.getClass());
        this.commandDelegateMap.put(message.getCommandID(), handler);
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

//            LOGGER.info("Raw message: " + serverResponse);
            if (!this.messageMap.containsKey(message.command)) {
                LOGGER.info("Unknown: {}", message.buildPrettyOutput());
                continue;
            }

            IMessage typedMessage = this.createTypedMessageFromCommandCode(message.command);
            if (typedMessage == null) {
                LOGGER.error("Failed to make a message for code. Not processing: {}", message.command);
                continue;
            }

            boolean wellFormed = typedMessage.isMessageWellFormed(message);
            if (!wellFormed) {
                LOGGER.error("Message was not well formed. Not processing: {}", serverResponse);
                continue;
            }

            // The IRCMessage being well formed guarantees that we can build() the correct typed message from it
            typedMessage.build(message);

            IMessageHandler messageHandler = this.commandDelegateMap.get(message.command);
            if (messageHandler == null) {
                LOGGER.info("{}: {}", message.command, messageGson.toJson(typedMessage));
                continue;
            }

            messageHandler.handleMessage(typedMessage);
        }
    }

    @Nullable
    public IMessage createTypedMessageFromCommandCode(@Nonnull String commandCode) {
        Class<? extends IMessage> clazzMessage = this.messageMap.get(commandCode);
        if (clazzMessage == null) {
            return null;
        }

        try {
            return clazzMessage.newInstance();
        } catch (InstantiationException e) {
            LOGGER.error("Failed to instantiate new message: {}", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to instantiate new message, access exception: {}", e);
        }

        return null;
    }

    public void postDisconnectedEvent() {
        this.eventBus.post(new ServerDisconnectedEvent());
    }

    public void cleanupOutgoingThread() {
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
