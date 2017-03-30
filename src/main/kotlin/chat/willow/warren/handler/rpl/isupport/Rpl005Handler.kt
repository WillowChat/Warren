package chat.willow.warren.handler.rpl.isupport

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl005Message
import chat.willow.warren.extension.monitor.MonitorState
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.ParsingState

class Rpl005Handler(val state: ParsingState,
                    val monitorState: MonitorState,
                    val prefixHandler: IRpl005PrefixHandler,
                    val channelModesHandler: IRpl005ChanModesHandler,
                    val channelTypesHandler: IRpl005ChanTypesHandler,
                    val caseMappingHandler: IRpl005CaseMappingHandler,
                    val monitorHandler: IRpl005MonitorHandler) : KaleHandler<Rpl005Message.Message>(Rpl005Message.Message.Parser) {

    private val LOGGER = loggerFor<Rpl005Handler>()


    override fun handle(message: Rpl005Message.Message, metadata: IMetadataStore) {
        LOGGER.debug("got isupport additions: ${message.tokens}")

        for ((key, value) in message.tokens) {
            if (value == null) {
                continue
            }

            when (key) {
                "PREFIX" -> prefixHandler.handle(value, state.userPrefixes)
                "CHANMODES" -> channelModesHandler.handle(value, state.channelModes)
                "CHANTYPES" -> channelTypesHandler.handle(value, state.channelTypes)
                "CASEMAPPING" -> caseMappingHandler.handle(value, state.caseMapping)
                "MONITOR" -> monitorHandler.handle(value, monitorState)
            }
        }
    }

}