package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.IKaleParsingStateDelegate
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.ircv3.CapLsMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.UserMessage
import engineer.carrot.warren.warren.handler.*
import engineer.carrot.warren.warren.handler.rpl.*
import engineer.carrot.warren.warren.handler.rpl.Rpl005.Rpl005ChanModesHandler
import engineer.carrot.warren.warren.handler.rpl.Rpl005.Rpl005ChanTypesHandler
import engineer.carrot.warren.warren.handler.rpl.Rpl005.Rpl005Handler
import engineer.carrot.warren.warren.handler.rpl.Rpl005.Rpl005PrefixHandler
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

interface IWarrenEvent {
    fun execute()
}

interface IWarrenEventSink {
    fun add(event: IWarrenEvent)
}

interface IWarrenEventSource {
    fun grab(): IWarrenEvent?
}

interface IWarrenEventQueue: IWarrenEventSource, IWarrenEventSink {
    fun clear()
}

class WarrenEventQueue: IWarrenEventQueue {
    private val queue = LinkedBlockingQueue<IWarrenEvent>(100)

    override fun add(event: IWarrenEvent) {
        queue.add(event)
    }

    override fun grab(): IWarrenEvent? {
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

class NewLineEvent(val line: String, val kale: IKale): IWarrenEvent {
    override fun execute() {
        kale.process(line)
    }

}

class SendSomethingEvent(val message: IMessage, val sink: IMessageSink): IWarrenEvent {
    override fun execute() {
        sink.write(message)
    }

}

interface IWarrenEventGenerator {
    fun run()
}

class NewLineWarrenEventGenerator(val queue: IWarrenEventQueue, val kale: IKale, val lineSource: ILineSource, val fireIncomingLineEvent: Boolean, val warrenEventDispatcher: IWarrenEventDispatcher?): IWarrenEventGenerator {

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
        } while(true)
    }

}

class IrcRunner(val eventDispatcher: IWarrenEventDispatcher, val kale: IKale, val sink: IMessageSink, val lineSource: ILineSource, val initialState: IrcState, val fireIncomingLineEvent: Boolean) : IIrcRunner, IKaleParsingStateDelegate {

    private val LOGGER = loggerFor<IrcRunner>()

    @Volatile var lastStateSnapshot: IrcState? = null
    private var eventQueue = WarrenEventQueue()
    var eventSink: IWarrenEventSink = eventQueue

    private lateinit var state: IrcState

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
        kale.register(JoinHandler(state.connection, state.channels))
        kale.register(KickHandler(state.connection, state.channels))
        kale.register(ModeHandler(eventDispatcher, state.parsing.channelTypes))
        kale.register(NickHandler(state.connection, state.channels))
        kale.register(NoticeHandler(state.parsing.channelTypes))
        kale.register(PartHandler(state.connection, state.channels))
        kale.register(PingHandler(sink))
        kale.register(PrivMsgHandler(eventDispatcher, state.parsing.channelTypes))
        kale.register(QuitHandler(eventDispatcher, state.connection, state.channels))
        kale.register(TopicHandler(state.channels))
        kale.register(Rpl005Handler(state.parsing, Rpl005PrefixHandler, Rpl005ChanModesHandler, Rpl005ChanTypesHandler))
        kale.register(Rpl332Handler(state.channels))
        kale.register(Rpl353Handler(state.channels, state.parsing.userPrefixes))
        kale.register(Rpl376Handler(eventDispatcher, sink, state.channels.joining.mapValues { entry -> entry.value.key }, state.connection))
        kale.register(Rpl471Handler(state.channels))
        kale.register(Rpl473Handler(state.channels))
        kale.register(Rpl474Handler(state.channels))
        kale.register(Rpl475Handler(state.channels))
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
            NewLineWarrenEventGenerator(eventQueue, kale, lineSource, fireIncomingLineEvent, eventDispatcher).run()
            LOGGER.warn("new line generator ended")

            eventQueue.clear()
            eventQueue.add(event = object : IWarrenEvent {
                override fun execute() {
                    state.connection.lifecycle = LifecycleState.DISCONNECTED
                }
            })
        }

        lineThread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, exception ->
            LOGGER.warn("uncaught exception in line generator, forcing a disconnect: $exception")

            eventQueue.clear()
            eventQueue.add(event = object : IWarrenEvent {
                override fun execute() {
                    state.connection.lifecycle = LifecycleState.DISCONNECTED
                }
            })
        }

        lineThread.start()

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
        } while(!shouldExit)

        if (lineThread.isAlive) {
            LOGGER.trace("line thread still alive - interrupting and waiting for 2 seconds")
            lineThread.interrupt()
            lineThread.join(2000)
        } else {
            LOGGER.trace("line thread not active - not killing it")
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