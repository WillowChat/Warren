package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.PrivMsgMessage
import engineer.carrot.warren.warren.*
import engineer.carrot.warren.warren.state.ChannelTypesState

class PrivMsgHandler(val eventDispatcher: IWarrenEventDispatcher, val channelTypesState: ChannelTypesState) : IKaleHandler<PrivMsgMessage> {
    private val LOGGER = loggerFor<PrivMsgHandler>()

    override val messageType = PrivMsgMessage::class.java

    override fun handle(message: PrivMsgMessage) {
        val source = message.source
        val target = message.target
        var messageContents = message.message

        if (source == null) {
            LOGGER.warn("got a PrivMsg but the source was missing - bailing: $message")
            return
        }

        var serverTime = ""
        if (message.time != null) {
            serverTime = "${message.time} "
        }

        var ctcp = CtcpEnum.NONE

        if (CtcpHelper.isMessageCTCP(messageContents)) {
            ctcp = CtcpEnum.from(messageContents)
            messageContents = CtcpHelper.trimCTCP(messageContents)

            if (ctcp === CtcpEnum.UNKNOWN) {
                LOGGER.warn("dropping unknown CTCP message: $target $messageContents")
                return
            }
        }

        if (channelTypesState.types.any { char -> target.startsWith(char) }) {
            // Channel message

            when (ctcp) {
                CtcpEnum.NONE -> {
                    eventDispatcher.fire(ChannelMessageEvent(user = source, channel = target, message = messageContents))

                    LOGGER.debug("$serverTime$target <${source.nick}> $messageContents")
                }

                CtcpEnum.ACTION -> {
                    eventDispatcher.fire(ChannelActionEvent(user = source, channel = target, message = messageContents))

                    LOGGER.debug("$serverTime$target ${source.nick} * $messageContents")
                }

                else -> Unit
            }
        } else {
            // Private message

            when (ctcp) {
                CtcpEnum.NONE -> {
                    eventDispatcher.fire(PrivateMessageEvent(user = source, message = messageContents))

                    LOGGER.debug("PM: $serverTime <${source.nick}> $messageContents")
                }

                CtcpEnum.ACTION -> {
                    eventDispatcher.fire(PrivateActionEvent(user = source, message = messageContents))

                    LOGGER.debug("PM: $serverTime ${source.nick} * $messageContents")
                }
            }
        }
    }
}