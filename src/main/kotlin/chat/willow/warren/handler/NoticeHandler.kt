package chat.willow.warren.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.NoticeMessage
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.ChannelTypesState

class NoticeHandler(val channelTypesState: ChannelTypesState) : KaleHandler<NoticeMessage.Message>(NoticeMessage.Message.Parser) {

    private val LOGGER = loggerFor<NoticeHandler>()


    override fun handle(message: NoticeMessage.Message, metadata: IMetadataStore) {
        val source = message.source
        val target = message.target
        val messageContents = message.message

        if (channelTypesState.types.any { char -> target.startsWith(char) }) {
            // Channel notice

            LOGGER.info("NOTICE: $target <${source.nick}> $messageContents")
        } else {
            // Private notice

            LOGGER.info("NOTICE PM: <${source.nick}> $messageContents")
        }
    }

}