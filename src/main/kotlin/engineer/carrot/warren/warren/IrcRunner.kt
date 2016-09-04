package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.IKaleParsingStateDelegate
import engineer.carrot.warren.kale.irc.message.ircv3.CapLsMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PingMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.UserMessage
import engineer.carrot.warren.warren.event.ConnectionLifecycleEvent
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.event.internal.*
import engineer.carrot.warren.warren.handler.*
import engineer.carrot.warren.warren.handler.rpl.*
import engineer.carrot.warren.warren.handler.rpl.Rpl005.*
import engineer.carrot.warren.warren.handler.sasl.AuthenticateHandler
import engineer.carrot.warren.warren.handler.sasl.Rpl903Handler
import engineer.carrot.warren.warren.handler.sasl.Rpl904Handler
import engineer.carrot.warren.warren.handler.sasl.Rpl905Handler
import engineer.carrot.warren.warren.state.IrcState
import engineer.carrot.warren.warren.state.LifecycleState
import kotlin.concurrent.thread

interface IIrcRunner {
    fun run()
}

class IrcRunner(val eventDispatcher: IWarrenEventDispatcher, private val internalEventQueue: IWarrenInternalEventQueue, val newLineGenerator: IWarrenInternalEventGenerator, val kale: IKale, val sink: IMessageSink, val initialState: IrcState) : IIrcRunner, IKaleParsingStateDelegate {

    private val LOGGER = loggerFor<IrcRunner>()

    @Volatile var lastStateSnapshot: IrcState? = null
    var eventSink: IWarrenInternalEventSink = internalEventQueue

    private lateinit var state: IrcState

    private val PONG_TIMER_MS: Long = 30 * 1000

    init {
        state = initialState
    }

    override fun run() {
        state = initialState

        if (!sink.setUp()) {
            LOGGER.warn("couldn't set up sink - bailing out")
            return
        }

        kale.parsingStateDelegate = this

        registerRFC1459Handlers()
        registerIrcV3Handlers()
        sendRegistrationMessages()
        runEventLoop()
    }

    private fun registerRFC1459Handlers() {
        kale.register(JoinHandler(state.connection, state.channels.joining, state.channels.joined, state.parsing.caseMapping))
        kale.register(KickHandler(state.connection, state.channels.joined, state.parsing.caseMapping))
        kale.register(ModeHandler(eventDispatcher, state.parsing.channelTypes, state.channels.joined, state.parsing.userPrefixes, state.parsing.caseMapping))
        kale.register(NickHandler(state.connection, state.channels.joined))
        kale.register(NoticeHandler(state.parsing.channelTypes))
        kale.register(PartHandler(state.connection, state.channels.joined, state.parsing.caseMapping))
        kale.register(PingHandler(sink, state.connection))
        kale.register(PongHandler(sink, state.connection))
        kale.register(PrivMsgHandler(eventDispatcher, state.channels.joined, state.parsing.channelTypes))
        kale.register(QuitHandler(eventDispatcher, state.connection, state.channels.joined))
        kale.register(TopicHandler(state.channels.joined, state.parsing.caseMapping))
        kale.register(Rpl005Handler(state.parsing, Rpl005PrefixHandler, Rpl005ChanModesHandler, Rpl005ChanTypesHandler, Rpl005CaseMappingHandler))
        kale.register(Rpl332Handler(state.channels.joined, state.parsing.caseMapping))
        kale.register(Rpl353Handler(state.channels.joined, state.parsing.userPrefixes, state.parsing.caseMapping))
        kale.register(Rpl376Handler(eventDispatcher, sink, state.channels.joining.all.mapValues { entry -> entry.value.key }, state.connection))
        kale.register(Rpl471Handler(state.channels.joining, state.parsing.caseMapping))
        kale.register(Rpl473Handler(state.channels.joining, state.parsing.caseMapping))
        kale.register(Rpl474Handler(state.channels.joining, state.parsing.caseMapping))
        kale.register(Rpl475Handler(state.channels.joining, state.parsing.caseMapping))
    }

    private fun registerIrcV3Handlers() {
        kale.register(AuthenticateHandler(state.connection.sasl, sink))
        kale.register(Rpl903Handler(state.connection.cap, state.connection.sasl, sink))
        kale.register(Rpl904Handler(state.connection.cap, state.connection.sasl, sink))
        kale.register(Rpl905Handler(state.connection.cap, state.connection.sasl, sink))
        kale.register(CapLsHandler(state.connection.cap, state.connection.sasl, sink))
        kale.register(CapAckHandler(state.connection.cap, state.connection.sasl, sink))
        kale.register(CapNakHandler(state.connection.cap, state.connection.sasl, sink))
    }

    private fun sendRegistrationMessages() {
        sink.write(CapLsMessage(caps = mapOf()))
        sink.write(NickMessage(nickname = state.connection.nickname))
        sink.write(UserMessage(username = state.connection.user, mode = "8", realname = state.connection.user))

        state.connection.lifecycle = LifecycleState.REGISTERING
        eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.REGISTERING))
    }

    private fun runEventLoop() {
        val lineThread = createLineThread(internalEventQueue, state)
        val pingThread = createPingThread(internalEventQueue, state, sink)

        lineThread.start()
        pingThread.start()

        eventLoop@ while (true) {
            val event = internalEventQueue.grab()

            if (Thread.currentThread().isInterrupted || event == null) {
                LOGGER.warn("interrupted or null event, bailing")
                break@eventLoop
            }

            event.execute()

            lastStateSnapshot = state.copy()

            if (state.connection.lifecycle == LifecycleState.DISCONNECTED) {
                eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.DISCONNECTED))

                LOGGER.trace("we disconnected, bailing")
                break@eventLoop
            }
        }

        if (lineThread.isAlive) {
            LOGGER.trace("line thread still alive - interrupting and assuming it'll bail out")
            lineThread.interrupt()
        } else {
            LOGGER.trace("line thread not active - not killing it")
        }

        if (pingThread.isAlive) {
            LOGGER.trace("Ping thread still alive - interrupting and assuming it'll bail out")
            pingThread.interrupt()
        } else {
            LOGGER.trace("ping thread not active - not killing it")
        }

        sink.tearDown()

        LOGGER.info("ending")
    }

    private fun createLineThread(eventQueue: IWarrenInternalEventQueue, state: IrcState): Thread {
        val lineThread = thread(start = false) {
            LOGGER.debug("new line thread starting up")
            newLineGenerator.run()
            LOGGER.warn("new line generator ended")

            eventQueue.clear()
            eventQueue.add {
                state.connection.lifecycle = LifecycleState.DISCONNECTED
            }
        }

        lineThread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, exception ->
            LOGGER.warn("uncaught exception in line generator, forcing a disconnect: $exception")

            eventQueue.clear()
            eventQueue.add {
                state.connection.lifecycle = LifecycleState.DISCONNECTED
            }
        }

        return lineThread
    }

    private fun createPingThread(eventQueue: IWarrenInternalEventQueue, state: IrcState, sink: IMessageSink): Thread {
        return thread(start = false) {
            pingLoop@ while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(10 * 1000)
                } catch(exception: InterruptedException) {
                    LOGGER.info("ping thread interrupted - bailing out")
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
        val prefixState = state.parsing.userPrefixes

        if (prefixState.prefixesToModes.containsValue(token)) {
            return true
        }

        val modesState = state.parsing.channelModes

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