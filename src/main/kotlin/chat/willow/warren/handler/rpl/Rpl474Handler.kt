package chat.willow.warren.handler.rpl

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl474Message
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoiningChannelLifecycle
import chat.willow.warren.state.JoiningChannelsState

class Rpl474Handler(val channelsState: JoiningChannelsState, val caseMappingState: CaseMappingState) : IKaleHandler<Rpl474Message> {

    private val LOGGER = loggerFor<Rpl474Handler>()

    override val messageType = Rpl474Message::class.java

    override fun handle(message: Rpl474Message, tags: ITagStore) {
        val channel = channelsState[message.channel]

        if (channel == null) {
            LOGGER.warn("got a banned from channel reply for a channel we don't think we're joining: $message")
            LOGGER.trace("channels state: $channelsState")
            return
        }

        LOGGER.warn("we are banned from channel, failed to join: $channel")
        channel.status = JoiningChannelLifecycle.FAILED

        LOGGER.trace("new channels state: $channelsState")
    }

}

