package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
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

interface IIrcRunner {
    fun run()
}

class IrcRunner(val eventDispatcher: IWarrenEventDispatcher, val kale: IKale, val sink: IMessageSink, val processor: IMessageProcessor, val initialState: IrcState) : IIrcRunner {

    @Volatile var lastStateSnapshot: IrcState? = null

    private lateinit var state: IrcState

    override fun run() {
        state = initialState

        registerHandlers()
        sendRegistrationMessages()
        processMessages()
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

    private fun processMessages() {
        do {
            val processed = processor.process()

            if (!processed) {
                println("processing message returned false, bailing")
                state.connection.lifecycle = LifecycleState.DISCONNECTED
                eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.DISCONNECTED))
                return
            }

            lastStateSnapshot = state.copy()

            if (state.connection.lifecycle == LifecycleState.DISCONNECTED) {
                println("we disconnected, bailing")
                return
            }
        } while (true)
    }
}