package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.IKaleParsingStateDelegate
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PingMessage
import engineer.carrot.warren.warren.event.ConnectionLifecycleEvent
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.event.internal.IWarrenInternalEventGenerator
import engineer.carrot.warren.warren.event.internal.IWarrenInternalEventQueue
import engineer.carrot.warren.warren.event.internal.IWarrenInternalEventSink
import engineer.carrot.warren.warren.extension.cap.CapManager
import engineer.carrot.warren.warren.extension.cap.CapState
import engineer.carrot.warren.warren.extension.sasl.SaslState
import engineer.carrot.warren.warren.handler.*
import engineer.carrot.warren.warren.handler.rpl.*
import engineer.carrot.warren.warren.handler.rpl.Rpl005.*
import engineer.carrot.warren.warren.helper.IExecutionContext
import engineer.carrot.warren.warren.helper.ISleeper
import engineer.carrot.warren.warren.helper.loggerFor
import engineer.carrot.warren.warren.registration.IRegistrationExtension
import engineer.carrot.warren.warren.registration.IRegistrationListener
import engineer.carrot.warren.warren.registration.IRegistrationManager
import engineer.carrot.warren.warren.registration.RFC1459RegistrationExtension
import engineer.carrot.warren.warren.state.AuthLifecycle
import engineer.carrot.warren.warren.state.IStateCapturing
import engineer.carrot.warren.warren.state.IrcState
import engineer.carrot.warren.warren.state.LifecycleState

interface IIrcConnection : IStateCapturing<IrcState> {

    fun run()

}

class IrcConnection(val eventDispatcher: IWarrenEventDispatcher, private val internalEventQueue: IWarrenInternalEventQueue, val newLineGenerator: IWarrenInternalEventGenerator, val kale: IKale, val sink: IMessageSink, initialState: IrcState, initialCapState: CapState, initialSaslState: SaslState, private val registrationManager: IRegistrationManager, private val sleeper: ISleeper, private val pingGeneratorExecutionContext: IExecutionContext, private val lineGeneratorExecutionContext: IExecutionContext) : IIrcConnection, IKaleParsingStateDelegate, IRegistrationListener {

    private val LOGGER = loggerFor<IrcConnection>()

    var eventSink: IWarrenInternalEventSink = internalEventQueue

    private var internalState = initialState
    @Volatile override var state: IrcState = initialState.copy()

    private val PONG_TIMER_MS: Long = 30 * 1000

    val caps = CapManager(initialCapState, kale, internalState.channels, initialSaslState, sink, internalState.parsing.caseMapping, registrationManager)
    lateinit var rfc1459RegistrationExtension: IRegistrationExtension

    override fun captureStateSnapshot() {
        state = internalState.copy()

        caps.captureStateSnapshot()
    }

    override fun run() {
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

    private fun registerRFC1459Handlers() {
        kale.register(JoinHandler(internalState.connection, internalState.channels.joining, internalState.channels.joined, internalState.parsing.caseMapping))
        kale.register(KickHandler(internalState.connection, internalState.channels.joined, internalState.parsing.caseMapping))
        kale.register(ModeHandler(eventDispatcher, internalState.parsing.channelTypes, internalState.channels.joined, internalState.parsing.userPrefixes, internalState.parsing.caseMapping))
        kale.register(NickHandler(internalState.connection, internalState.channels.joined))
        kale.register(NoticeHandler(internalState.parsing.channelTypes))
        kale.register(PartHandler(internalState.connection, internalState.channels.joined, internalState.parsing.caseMapping))
        kale.register(PingHandler(sink, internalState.connection))
        kale.register(PongHandler(sink, internalState.connection))
        kale.register(PrivMsgHandler(eventDispatcher, internalState.channels.joined, internalState.parsing.channelTypes))
        kale.register(QuitHandler(eventDispatcher, internalState.connection, internalState.channels.joined))
        kale.register(TopicHandler(internalState.channels.joined, internalState.parsing.caseMapping))
        kale.register(Rpl005Handler(internalState.parsing, Rpl005PrefixHandler, Rpl005ChanModesHandler, Rpl005ChanTypesHandler, Rpl005CaseMappingHandler))
        kale.register(Rpl332Handler(internalState.channels.joined, internalState.parsing.caseMapping))
        kale.register(Rpl353Handler(internalState.channels.joined, internalState.parsing.userPrefixes, internalState.parsing.caseMapping))
        kale.register(Rpl376Handler(sink, caps.internalState, rfc1459RegistrationExtension, caps))
        kale.register(Rpl471Handler(internalState.channels.joining, internalState.parsing.caseMapping))
        kale.register(Rpl473Handler(internalState.channels.joining, internalState.parsing.caseMapping))
        kale.register(Rpl474Handler(internalState.channels.joining, internalState.parsing.caseMapping))
        kale.register(Rpl475Handler(internalState.channels.joining, internalState.parsing.caseMapping))
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
                sink.write(JoinMessage(channels = listOf(channel), keys = listOf(key)))
            } else {
                sink.write(JoinMessage(channels = listOf(channel)))
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
                            sink.write(PingMessage(token = "$currentTime"))
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