package engineer.carrot.warren.warren;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import engineer.carrot.warren.warren.irc.handlers.IMessageHandler;
import engineer.carrot.warren.warren.irc.handlers.RPL.*;
import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.IISupportManager;
import engineer.carrot.warren.warren.irc.handlers.RPL.isupport.ISupportHandler;
import engineer.carrot.warren.warren.irc.handlers.core.*;
import engineer.carrot.warren.warren.irc.handlers.core.mode.ModeHandler;
import engineer.carrot.warren.warren.irc.handlers.multi.IMotdMultiHandler;
import engineer.carrot.warren.warren.irc.handlers.multi.MotdMultiHandler;
import engineer.carrot.warren.warren.irc.messages.IMessage;
import engineer.carrot.warren.warren.irc.messages.JavaIrcMessage;
import engineer.carrot.warren.warren.irc.messages.RPL.*;
import engineer.carrot.warren.warren.irc.messages.core.*;
import engineer.carrot.warren.warren.util.IMessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class IncomingHandler implements IIncomingHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(IncomingHandler.class);
    private final Gson messageGson = new Gson();

    private final IWarrenDelegate botDelegate;
    private final IMessageQueue outgoingQueue;
    private final IEventSink eventSink;

    private Map<String, Optional<IMessageHandler>> messageHandlers;
    private Map<String, Class<? extends IMessage>> messages;

    private Set<String> nextExpectedCommands;

    // Handlers
    private ISupportHandler iSupportHandler;

    // Multi Handlers
    private IMotdMultiHandler motdHandler;

    public IncomingHandler(IWarrenDelegate botDelegate, IMessageQueue outgoingQueue, IEventSink eventSink) {
        this.botDelegate = botDelegate;
        this.outgoingQueue = outgoingQueue;
        this.eventSink = eventSink;

        this.initialise();
    }

    private void initialise() {
        this.messages = Maps.newHashMap();
        this.messageHandlers = Maps.newHashMap();

        this.nextExpectedCommands = Sets.newHashSet();
        this.iSupportHandler = new ISupportHandler(this.botDelegate.getUserManager());

        this.addInternalHandlers();

        this.initialiseMessageHandlers();
    }

    private void addInternalHandlers() {
        this.addMessageHandler(new CreatedMessage(), Optional.<IMessageHandler>absent());
        this.addMessageHandler(new NoticeMessage(), Optional.<IMessageHandler>absent());
        this.addMessageHandler(new PongMessage(), Optional.<IMessageHandler>absent());
        this.addMessageHandler(new TopicWhoTimeMessage(), Optional.<IMessageHandler>absent());
        this.addMessageHandler(new YourHostMessage(), Optional.<IMessageHandler>absent());

        this.addMessageHandler(new MotdStartMessage(), Optional.<IMessageHandler>of(new MotdStartHandler()));
        this.addMessageHandler(new MotdMessage(), Optional.<IMessageHandler>of(new MotdHandler()));
        this.addMessageHandler(new MotdEndMessage(), Optional.<IMessageHandler>of(new MotdEndHandler()));
        this.addMessageHandler(new NoTopicMessage(), Optional.<IMessageHandler>of(new NoTopicHandler()));
        this.addMessageHandler(new TopicMessage(), Optional.<IMessageHandler>of(new TopicHandler()));
        this.addMessageHandler(new WelcomeMessage(), Optional.<IMessageHandler>of(new WelcomeHandler()));
        this.addMessageHandler(new JoinedChannelMessage(), Optional.<IMessageHandler>of(new JoinHandler()));
        this.addMessageHandler(new PartChannelMessage(), Optional.<IMessageHandler>of(new PartHandler()));
        this.addMessageHandler(new PingMessage(), Optional.<IMessageHandler>of(new PingHandler()));
        this.addMessageHandler(new PrivMsgMessage(), Optional.<IMessageHandler>of(new PrivMsgHandler()));
        this.addMessageHandler(new NamReplyMessage(), Optional.<IMessageHandler>of(new NamReplyHandler()));
        this.addMessageHandler(new ChangeNicknameMessage(), Optional.<IMessageHandler>of(new ChangeNicknameHandler()));
        this.addMessageHandler(new ModeMessage(), Optional.<IMessageHandler>of(new ModeHandler()));
        this.addMessageHandler(new ISupportMessage(), Optional.<IMessageHandler>of(this.iSupportHandler));
    }

    private void initialiseMessageHandlers() {
        for (Optional<IMessageHandler> optionalHandler : this.messageHandlers.values()) {
            if (!optionalHandler.isPresent()) {
                continue;
            }

            IMessageHandler handler = optionalHandler.get();
            handler.setBotDelegate(this.botDelegate);
            handler.setOutgoingQueue(this.outgoingQueue);
            handler.setIncomingHandler(this);
            handler.setEventSink(this.eventSink);
        }

        this.initialiseMultiMessageHandlers();

        for (Optional<IMessageHandler> optionalHandler : this.messageHandlers.values()) {
            if (!optionalHandler.isPresent()) {
                continue;
            }

            optionalHandler.get().initialise();
        }
    }

    private void initialiseMultiMessageHandlers() {
        this.motdHandler = new MotdMultiHandler();
    }

    @Nullable
    private IMessage createTypedMessageFromCommandCode(String commandCode) {
        Class<? extends IMessage> clazzMessage = this.messages.get(commandCode);
        if (clazzMessage == null) {
            return null;
        }

        try {
            IMessage message = clazzMessage.newInstance();
            message.setISupportManager(this.iSupportHandler);
            return message;
        } catch (InstantiationException e) {
            LOGGER.error("Failed to instantiate new message: {}", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to instantiate new message, access exception: {}", e);
        }

        return null;
    }

    @Override
    public void setNextExpectedCommands(Set<String> nextExpectedCommands) {
        this.nextExpectedCommands = nextExpectedCommands;
    }

    @Override
    public void setNextExpectedCommandToAnything() {
        this.nextExpectedCommands.clear();
    }

    @Override
    public boolean handleIRCMessage(JavaIrcMessage message, String originalLine) {
        if (!this.nextExpectedCommands.isEmpty()) {
            // Check that this command is in the set of commands we're expecting next
            // LOGGER.info("Next expected commands: " + this.nextExpectedCommands);

            if (!this.nextExpectedCommands.contains(message.command)) {
                LOGGER.warn("Wasn't expecting '{}' next - expect unexpected behaviour");
            }
        }
//            LOGGER.info("Raw message: " + serverResponse);
        if (!this.messages.containsKey(message.command)) {
            LOGGER.info("Unknown: {}", message.buildPrettyOutput());
            return true;
        }

        IMessage typedMessage = this.createTypedMessageFromCommandCode(message.command);
        if (typedMessage == null) {
            LOGGER.error("Failed to make a message for code. Not processing: {}", message.command);
            return false;
        }

        IMessage builtMessage = typedMessage.build(message);
        if (builtMessage == null) {
            LOGGER.error("Failed to populate message from IRC message: {}", message);
            return false;
        }

        Optional<IMessageHandler> optionalMessageHandler = this.messageHandlers.get(message.command);
        if (!optionalMessageHandler.isPresent()) {
            LOGGER.info("{}: {}", message.command, messageGson.toJson(typedMessage));
            return true;
        }

        optionalMessageHandler.get().handleMessage(typedMessage);

        return true;
    }

    // Multi message handlers

    @Override
    public IMotdMultiHandler getMotdHandler() {
        return this.motdHandler;
    }

    @Override
    public IISupportManager getISupportManager() {
        return this.iSupportHandler;
    }

    // API

    @Override
    public void addMessageHandler(IMessage message, Optional<IMessageHandler> handler) {
        String command = message.getCommand();

        if (this.messages.containsKey(command)) {
            throw new RuntimeException("Message with code " + command + " is already known to this IncomingHandler!");
        }

        this.messages.put(command, message.getClass());
        this.messageHandlers.put(command, handler);
    }
}
