package chat.willow.warren

import chat.willow.kale.IKale
import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleParsingStateDelegate
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.rfc1459.*
import chat.willow.kale.irc.message.rfc1459.rpl.*
import chat.willow.warren.event.ConnectionLifecycleEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.event.internal.IWarrenInternalEventGenerator
import chat.willow.warren.event.internal.IWarrenInternalEventQueue
import chat.willow.warren.event.internal.SendSomethingEvent
import chat.willow.warren.extension.cap.CapManager
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.monitor.MonitorState
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.handler.*
import chat.willow.warren.handler.rpl.*
import chat.willow.warren.handler.rpl.isupport.*
import chat.willow.warren.helper.IExecutionContext
import chat.willow.warren.helper.ISleeper
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.registration.IRegistrationExtension
import chat.willow.warren.registration.IRegistrationListener
import chat.willow.warren.registration.IRegistrationManager
import chat.willow.warren.registration.RFC1459RegistrationExtension
import chat.willow.warren.state.AuthLifecycle
import chat.willow.warren.state.IStateCapturing
import chat.willow.warren.state.IrcState
import chat.willow.warren.state.LifecycleState
import java.util.concurrent.atomic.AtomicBoolean

interface IIrcConnection : IStateCapturing<IrcState>, IClientMessageSending, IStringMessageSending {

    val caps: IStateCapturing<CapState>

    fun start()

}

class IrcConnection(val eventDispatcher: IWarrenEventDispatcher, private val internalEventQueue: IWarrenInternalEventQueue, val newLineGenerator: IWarrenInternalEventGenerator, val kale: IKale, val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, val sink: IMessageSink, initialState: IrcState, initialCapState: CapState, initialSaslState: SaslState, initialMonitorState: MonitorState, private val registrationManager: IRegistrationManager, private val sleeper: ISleeper, private val pingGeneratorExecutionContext: IExecutionContext, private val lineGeneratorExecutionContext: IExecutionContext) : IIrcConnection, IKaleParsingStateDelegate, IRegistrationListener {

    private val LOGGER = loggerFor<IrcConnection>()

    private var hasSetUp = AtomicBoolean(false)
    private var internalState = initialState
    @Volatile override var state: IrcState = initialState.copy()

    private val PONG_TIMER_MS: Long = 30 * 1000

    override val caps = CapManager(initialCapState, kaleRouter, internalState.channels, initialSaslState, initialMonitorState, sink, internalState.parsing.caseMapping, registrationManager, eventDispatcher)
    private lateinit var rfc1459RegistrationExtension: IRegistrationExtension

    override fun captureStateSnapshot() {
        state = internalState.copy()

        caps.captureStateSnapshot()
    }

    override fun start() {
        if (!hasSetUp.compareAndSet(false, true)) {
            LOGGER.error("can't run IrcConnections twice - make a new one")
            return
        }

        if (!sink.setUp()) {
            LOGGER.warn("couldn't set up sink - bailing out")
            return
        }

        kale.parsingStateDelegate = this

        rfc1459RegistrationExtension = RFC1459RegistrationExtension(sink = sink, nickname = internalState.connection.nickname, username = internalState.connection.user, password = internalState.connection.password, registrationManager = registrationManager)

        registerRFC1459Handlers()
        caps.setUp()

        registrationManager.register(caps)
        registrationManager.register(rfc1459RegistrationExtension)

        internalState.connection.lifecycle = LifecycleState.REGISTERING
        eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.REGISTERING))

        registrationManager.startRegistration()

        runEventLoop()
    }

    override fun send(message: String) {
        internalEventQueue.add {
            sink.writeRaw(message)
        }
    }

    override fun <M : Any> send(message: M) {
        internalEventQueue.add(SendSomethingEvent(message, sink = sink))
    }

    override fun send(message: String, target: String) {
        send(PrivMsgMessage.Command(target = target, message = message))
    }

    private fun registerRFC1459Handlers() {
        kaleRouter.register(JoinMessage.command, JoinHandler(internalState.connection, internalState.channels.joining, internalState.channels.joined, internalState.parsing.caseMapping))
        kaleRouter.register(KickMessage.command, KickHandler(internalState.connection, internalState.channels.joined, internalState.parsing.caseMapping))
        kaleRouter.register(ModeMessage.command, ModeHandler(eventDispatcher, this, internalState.parsing.channelTypes, internalState.channels.joined, internalState.parsing.userPrefixes, internalState.parsing.caseMapping))
        kaleRouter.register(NickMessage.command, NickHandler(internalState.connection, internalState.channels.joined, internalState.parsing.caseMapping))
        kaleRouter.register(NoticeMessage.command, NoticeHandler(internalState.parsing.channelTypes))
        kaleRouter.register(PartMessage.command, PartHandler(internalState.connection, internalState.channels.joined, internalState.parsing.caseMapping))
        kaleRouter.register(PingMessage.command, PingHandler(sink, internalState.connection))
        kaleRouter.register(PongMessage.command, PongHandler(sink, internalState.connection))
        kaleRouter.register(PrivMsgMessage.command, PrivMsgHandler(eventDispatcher, this, internalState.channels.joined, internalState.parsing.channelTypes))
        kaleRouter.register(QuitMessage.command, QuitHandler(eventDispatcher, internalState.connection, internalState.channels.joined))
        kaleRouter.register(TopicMessage.command, TopicHandler(internalState.channels.joined, internalState.parsing.caseMapping))
        kaleRouter.register(Rpl005Message.command, Rpl005Handler(internalState.parsing, caps.monitor.internalState, Rpl005PrefixHandler, Rpl005ChanModesHandler, Rpl005ChanTypesHandler, Rpl005CaseMappingHandler, Rpl005MonitorHandler(caps)))
        kaleRouter.register(Rpl332Message.command, Rpl332Handler(internalState.channels.joined, internalState.parsing.caseMapping))
        kaleRouter.register(Rpl353Message.command, Rpl353Handler(internalState.channels.joined, internalState.parsing.userPrefixes, internalState.parsing.caseMapping))
        kaleRouter.register(Rpl376Message.command, Rpl376Handler(sink, caps.internalState, rfc1459RegistrationExtension, caps))
        kaleRouter.register(Rpl471Message.command, Rpl471Handler(internalState.channels.joining, internalState.parsing.caseMapping))
        kaleRouter.register(Rpl473Message.command, Rpl473Handler(internalState.channels.joining, internalState.parsing.caseMapping))
        kaleRouter.register(Rpl474Message.command, Rpl474Handler(internalState.channels.joining, internalState.parsing.caseMapping))
        kaleRouter.register(Rpl475Message.command, Rpl475Handler(internalState.channels.joining, internalState.parsing.caseMapping))
    }

    override fun onRegistrationEnded() {
        LOGGER.info("registration ended")

        if (internalState.connection.lifecycle == LifecycleState.CONNECTED) {
            LOGGER.warn("already connected - not ending registration again")
            return
        }

        when (internalState.connection.lifecycle) {
            LifecycleState.CONNECTING, LifecycleState.REGISTERING -> nickservAuthIfRequested()
            else -> LOGGER.warn("Registration ended, but we didn't think we were connecting - not authing")
        }

        if (internalState.connection.nickServ.shouldAuth && internalState.connection.nickServ.lifecycle == AuthLifecycle.AUTHED) {
            LOGGER.debug("waiting ${internalState.connection.nickServ.channelJoinWaitSeconds} seconds before joining channels")

            val slept = sleeper.sleep(internalState.connection.nickServ.channelJoinWaitSeconds * 1000L)
            if (!slept) {
                LOGGER.warn("interrupted whilst waiting to join channels - bailing out")
                return
            }
        }

        val channelsToJoin = internalState.channels.joining.all.mapValues { entry -> entry.value.key }
        LOGGER.debug("joining channels: $channelsToJoin")

        join(channelsToJoin, sink)

        internalState.connection.lifecycle = LifecycleState.CONNECTED
        eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.CONNECTED))
    }

    private fun nickservAuthIfRequested() {
        if (!internalState.connection.nickServ.shouldAuth) {
            return
        }

        val credentials = internalState.connection.nickServ.credentials
        if (credentials == null) {
            LOGGER.warn("asked to auth, but given no credentials, marking auth failed")

            internalState.connection.nickServ.lifecycle = AuthLifecycle.AUTH_FAILED
        } else {
            LOGGER.debug("authing with nickserv - assuming success as replies aren't standardised (use SASL instead if you can)")

            sink.writeRaw("NICKSERV identify ${credentials.account} ${credentials.password}")
            internalState.connection.nickServ.lifecycle = AuthLifecycle.AUTHED
        }
    }

    private fun join(channelsWithKeys: Map<String, String?>, sink: IMessageSink) {
        for ((channel, key) in channelsWithKeys) {
            if (key != null) {
                sink.write(JoinMessage.Command(channels = listOf(channel), keys = listOf(key)))
            } else {
                sink.write(JoinMessage.Command(channels = listOf(channel)))
            }
        }
    }

    override fun onRegistrationFailed() {
        LOGGER.info("registration failed")
    }

    private fun runEventLoop() {
        val lineGeneratorBlock = lineGeneratorBlock(internalEventQueue, internalState)
        val pingGeneratorBlock = pingGeneratorBlock(internalEventQueue, internalState, sink)

        lineGeneratorExecutionContext.execute(lineGeneratorBlock)
        pingGeneratorExecutionContext.execute(pingGeneratorBlock)

        eventLoop@ while (true) {
            val event = internalEventQueue.grab()

            if (Thread.currentThread().isInterrupted || event == null) {
                LOGGER.warn("interrupted or null event, bailing")
                break@eventLoop
            }

            event.execute()

            synchronized(state) {
                captureStateSnapshot()
            }

            if (internalState.connection.lifecycle == LifecycleState.DISCONNECTED) {
                eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.DISCONNECTED))

                LOGGER.trace("we disconnected, bailing")
                break@eventLoop
            }
        }

        lineGeneratorExecutionContext.tearDown()
        pingGeneratorExecutionContext.tearDown()

        sink.tearDown()

        LOGGER.info("ending")
    }

    private fun lineGeneratorBlock(eventQueue: IWarrenInternalEventQueue, state: IrcState): () -> Unit {
        return {
            LOGGER.debug("new line thread starting up")
            newLineGenerator.run()
            LOGGER.warn("new line generator ended")

            eventQueue.clear()
            eventQueue.add {
                state.connection.lifecycle = LifecycleState.DISCONNECTED
            }
        }
    }

    private fun pingGeneratorBlock(eventQueue: IWarrenInternalEventQueue, state: IrcState, sink: IMessageSink): () -> Unit {
        return {
            pingLoop@ while (true) {
                val slept = sleeper.sleep(10 * 1000)
                if (!slept) {
                    LOGGER.info("ping sleep failed - bailing out")
                    break@pingLoop
                }

                eventQueue.add {
                    if (state.connection.lifecycle == LifecycleState.CONNECTED) {
                        val currentTime = System.currentTimeMillis()

                        val msSinceLastPing = currentTime - state.connection.lastPingOrPong
                        if (msSinceLastPing > PONG_TIMER_MS) {
                            sink.write(PingMessage.Command(token = "$currentTime"))
                        }
                    }
                }
            }
        }
    }

    // IKaleParsingStateDelegate

    override fun modeTakesAParameter(isAdding: Boolean, token: Char): Boolean {
        val prefixState = internalState.parsing.userPrefixes

        if (prefixState.prefixesToModes.containsValue(token)) {
            return true
        }

        val modesState = internalState.parsing.channelModes

        if (modesState.typeD.contains(token)) {
            return false
        }

        if (modesState.typeA.contains(token) || modesState.typeB.contains(token)) {
            return true
        }

        if (isAdding) {
            return modesState.typeC.contains(token)
        }

        return false
    }

}