package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.UserMessage
import engineer.carrot.warren.warren.handler.PingHandler
import engineer.carrot.warren.warren.handler.Rpl005.Rpl005ChanModesHandler
import engineer.carrot.warren.warren.handler.Rpl005.Rpl005Handler
import engineer.carrot.warren.warren.handler.Rpl005.Rpl005PrefixHandler
import engineer.carrot.warren.warren.handler.Rpl376Handler
import engineer.carrot.warren.warren.state.IrcState

interface IIrcRunner {
    fun run()
}

class IrcRunner(val kale: IKale, val sink: IMessageSink, val processor: IMessageProcessor, val initialState: IrcState) : IIrcRunner {

    private lateinit var state: IrcState

    override fun run() {
        state = initialState

        registerHandlers()
        sendRegistrationMessages()
        processMessages()
    }

    private fun registerHandlers() {
        kale.register(PingHandler(sink))
        kale.register(Rpl005Handler(state.parsing, Rpl005PrefixHandler, Rpl005ChanModesHandler))
        kale.register(Rpl376Handler(sink, channelsToJoin = listOf("#carrot", "#botdev")))
    }

    private fun sendRegistrationMessages() {
        sink.write(NickMessage(nickname = state.connection.nickname))
        sink.write(UserMessage(username = state.connection.username, mode = "8", realname = state.connection.username))
    }

    private fun processMessages() {
        do {
            val processed = processor.process()

            if (!processed) {
                println("processing message returned false, bailing")
                return
            }
        } while (true)
    }
}