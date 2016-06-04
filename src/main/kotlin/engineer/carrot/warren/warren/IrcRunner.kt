package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.IKaleParsingStateDelegate
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapLsMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PingMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.UserMessage
import engineer.carrot.warren.warren.handler.*
import engineer.carrot.warren.warren.handler.rpl.*
import engineer.carrot.warren.warren.handler.rpl.Rpl005.*
import engineer.carrot.warren.warren.handler.sasl.AuthenticateHandler
import engineer.carrot.warren.warren.handler.sasl.Rpl903Handler
import engineer.carrot.warren.warren.handler.sasl.Rpl904Handler
import engineer.carrot.warren.warren.handler.sasl.Rpl905Handler
import engineer.carrot.warren.warren.state.IrcState
import engineer.carrot.warren.warren.state.LifecycleState
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

interface IIrcRunner {
    fun run()
}

interface IWarrenInternalEvent {
    fun execute()
}

interface IWarrenInternalEventSink {
    fun add(event: IWarrenInternalEvent)
}

interface IWarrenInternalEventSource {
    fun grab(): IWarrenInternalEvent?
}

interface IWarrenInternalEventQueue : IWarrenInternalEventSource, IWarrenInternalEventSink {
    fun clear()
}

class WarrenInternalEventQueue : IWarrenInternalEventQueue {
    private val queue = LinkedBlockingQueue<IWarrenInternalEvent>(100)

    override fun add(event: IWarrenInternalEvent) {
        queue.add(event)
    }

    override fun grab(): IWarrenInternalEvent? {
        try {
            return queue.take()
        } catch (e: InterruptedException) {
            return null
        }
    }

    override fun clear() {
        queue.clear()
    }

}

class NewLineEvent(val line: String, val kale: IKale) : IWarrenInternalEvent {
    override fun execute() {
        kale.process(line)
    }

}

class SendSomethingEvent(val message: Any, val sink: IMessageSink) : IWarrenInternalEvent {
    override fun execute() {
        sink.write(message)
    }

}

interface IWarrenInternalEventGenerator {
    fun run()
}

class NewLineWarrenEventGenerator(val queue: IWarrenInternalEventQueue, val kale: IKale, val lineSource: ILineSource, val fireIncomingLineEvent: Boolean, val warrenEventDispatcher: IWarrenEventDispatcher?) : IWarrenInternalEventGenerator {

    private val LOGGER = loggerFor<NewLineWarrenEventGenerator>()

    override fun run() {
        do {
            val line = lineSource.nextLine()
            if (line == null) {
                LOGGER.trace("got null line, bailing out")
                return
            } else {
                LOGGER.trace("added to queue: $line")

                if (fireIncomingLineEvent && warrenEventDispatcher != null) {
                    warrenEventDispatcher.fire(RawIncomingLineEvent(line = line))
                }

                queue.add(NewLineEvent(line, kale))
            }
        } while (true)
    }

}

class IrcRunner(val eventDispatcher: IWarrenEventDispatcher, val kale: IKale, val sink: IMessageSink, val lineSource: ILineSource, val initialState: IrcState, val fireIncomingLineEvent: Boolean) : IIrcRunner, IKaleParsingStateDelegate {

    private val LOGGER = loggerFor<IrcRunner>()

    @Volatile var lastStateSnapshot: IrcState? = null
    private var eventQueue = WarrenInternalEventQueue()
    var eventSink: IWarrenInternalEventSink = eventQueue

    private lateinit var state: IrcState

    private val PONG_TIMER_MS: Long = 30 * 1000

    init {
        state = initialState
    }

    override fun run() {
        state = initialState

        sink.setUp()

        kale.parsingStateDelegate = this

        registerHandlers()
        sendRegistrationMessages()
        startEventQueue()
    }

    private fun registerHandlers() {
        kale.register(AuthenticateHandler(state.connection.sasl, sink))
        kale.register(Rpl903Handler(state.connection.cap, state.connection.sasl, sink))
        kale.register(Rpl904Handler(state.connection.cap, state.connection.sasl, sink))
        kale.register(Rpl905Handler(state.connection.cap, state.connection.sasl, sink))
        kale.register(CapLsHandler(state.connection.cap, state.connection.sasl, sink))
        kale.register(CapAckHandler(state.connection.cap, state.connection.sasl, sink))
        kale.register(CapNakHandler(state.connection.cap, state.connection.sasl, sink))
        kale.register(JoinHandler(state.connection, state.channels.joining, state.channels.joined, state.parsing.caseMapping))
        kale.register(KickHandler(state.connection, state.channels.joined, state.parsing.caseMapping))
        kale.register(ModeHandler(eventDispatcher, state.parsing.channelTypes, state.channels.joined, state.parsing.userPrefixes, state.parsing.caseMapping))
        kale.register(NickHandler(state.connection, state.channels.joined))
        kale.register(NoticeHandler(state.parsing.channelTypes))
        kale.register(PartHandler(state.connection, state.channels.joined, state.parsing.caseMapping))
        kale.register(PingHandler(sink, state.connection))
        kale.register(PongHandler(sink, state.connection))
        kale.register(PrivMsgHandler(eventDispatcher, state.parsing.channelTypes))
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

    private fun sendRegistrationMessages() {
        sink.write(CapLsMessage(caps = mapOf()))
        sink.write(NickMessage(nickname = state.connection.nickname))
        sink.write(UserMessage(username = state.connection.username, mode = "8", realname = state.connection.username))

        state.connection.lifecycle = LifecycleState.REGISTERING
        eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.REGISTERING))
    }

    private fun startEventQueue() {
        val lineThread = thread(start = false) {
            LOGGER.debug("new line thread starting up")
            NewLineWarrenEventGenerator(eventQueue, kale, lineSource, fireIncomingLineEvent, eventDispatcher).run()
            LOGGER.warn("new line generator ended")

            eventQueue.clear()
            eventQueue.add(event = object : IWarrenInternalEvent {
                override fun execute() {
                    state.connection.lifecycle = LifecycleState.DISCONNECTED
                }
            })
        }

        lineThread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, exception ->
            LOGGER.warn("uncaught exception in line generator, forcing a disconnect: $exception")

            eventQueue.clear()
            eventQueue.add(event = object : IWarrenInternalEvent {
                override fun execute() {
                    state.connection.lifecycle = LifecycleState.DISCONNECTED
                }
            })
        }

        val pingThread = thread(start = false) {
            pingLoop@ while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(10 * 1000)
                } catch(exception: InterruptedException) {
                    LOGGER.info("ping thread interrupted - bailing out")
                    break@pingLoop
                }

                eventQueue.add(event = object : IWarrenInternalEvent {
                    override fun execute() {
                        if (state.connection.lifecycle == LifecycleState.CONNECTED) {
                            val currentTime = System.currentTimeMillis()

                            val msSinceLastPing = currentTime - state.connection.lastPingOrPong
                            if (msSinceLastPing > PONG_TIMER_MS) {
                                sink.write(PingMessage(token = "$currentTime"))
                            }
                        }
                    }
                })
            }
        }

        lineThread.start()
        pingThread.start()

        var shouldExit = false

        do {
            val event = eventQueue.grab()
            if (Thread.currentThread().isInterrupted || event == null) {
                LOGGER.warn("got null event, bailing")
                shouldExit = true
            } else {
                event.execute()

                lastStateSnapshot = state.copy()

                if (state.connection.lifecycle == LifecycleState.DISCONNECTED) {
                    eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.DISCONNECTED))
                    LOGGER.trace("we disconnected, bailing")
                    shouldExit = true
                }
            }
        } while (!shouldExit)

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