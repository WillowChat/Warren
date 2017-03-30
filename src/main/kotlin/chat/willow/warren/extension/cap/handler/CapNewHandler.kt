package chat.willow.warren.extension.cap.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.extension.cap.CapMessage
import chat.willow.warren.IMessageSink
import chat.willow.warren.extension.cap.CapState
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.helper.loggerFor

class CapNewHandler(val capState: CapState, val sink: IMessageSink, val capManager: ICapManager) : KaleHandler<CapMessage.New.Message>(CapMessage.New.Message.Parser) {

    private val LOGGER = loggerFor<CapNewHandler>()


    override fun handle(message: CapMessage.New.Message, metadata: IMetadataStore) {
        val caps = message.caps

        LOGGER.trace("server NEWed following caps: $caps")

        val reqCaps = caps.keys.filter { capState.negotiate.contains(it) && !capState.accepted.contains(it) }
        if (reqCaps.isNotEmpty()) {
            LOGGER.trace("REQing newly advertised caps $reqCaps")
            sink.write(CapMessage.Req.Command(caps = reqCaps))
        }

        message.caps.forEach { capManager.capValueSet(it.key, it.value) }
    }

}

