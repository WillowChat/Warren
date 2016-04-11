package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.irc.message.rfc1459.*
import engineer.carrot.warren.warren.handler.PingHandler
import engineer.carrot.warren.warren.handler.Rpl376Handler

class IrcRunner(val connectionInfo: ConnectionInfo, val kale: IKale, val sink: IMessageSink, val processor: IMessageProcessor): IIrcRunner {

    override fun run() {
        registerHandlers()
        sendRegistrationMessages()
        processMessages()
    }

    private fun registerHandlers() {
        kale.register(PingHandler(sink))
        kale.register(Rpl376Handler(sink))
    }

    private fun sendRegistrationMessages() {
        sink.write(NickMessage(nickname = connectionInfo.nickname))
        sink.write(UserMessage(username = connectionInfo.nickname, mode = "8", realname = connectionInfo.nickname))
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