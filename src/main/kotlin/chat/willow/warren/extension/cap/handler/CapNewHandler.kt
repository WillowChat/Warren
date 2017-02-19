package chat.willow.warren.extension.cap.handler

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.extension.cap.CapNewMessage
import chat.willow.kale.irc.message.extension.cap.CapReqMessage
import chat.willow.kale.irc.message.extension.sasl.AuthenticateMessage
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapLifecycle
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.sasl.SaslState
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.AuthLifecycle

class CapNewHandler(val capState: CapState, val sink: IMessageSink) : IKaleHandler<CapNewMessage> {

    private val LOGGER = loggerFor<CapNewHandler>()

    override val messageType = CapNewMessage::class.java

    override fun handle(message: CapNewMessage, tags: ITagStore) {
        val caps = message.caps

        LOGGER.trace("server NEWed following caps: $caps")

        val reqCaps = caps.keys.filter { capState.negotiate.contains(it) && !capState.accepted.contains(it) }
        if (reqCaps.isNotEmpty()) {
            LOGGER.trace("REQing newly advertised caps $reqCaps")
            sink.write(CapReqMessage(caps = reqCaps))
        }
    }

}

