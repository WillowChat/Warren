package chat.willow.warren.handler

import chat.willow.kale.IMetadataStore
import chat.willow.kale.KaleHandler
import chat.willow.kale.irc.message.rfc1459.QuitMessage
import chat.willow.warren.event.ConnectionLifecycleEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.ConnectionState
import chat.willow.warren.state.JoinedChannelsState
import chat.willow.warren.state.LifecycleState

class QuitHandler(val eventDispatcher: IWarrenEventDispatcher, val connectionState: ConnectionState, val channelsState: JoinedChannelsState) : KaleHandler<QuitMessage.Message>(QuitMessage.Message.Parser) {

    private val LOGGER = loggerFor<QuitHandler>()


    override fun handle(message: QuitMessage.Message, metadata: IMetadataStore) {
        val from = message.source.nick

        if (from == connectionState.nickname) {
            // We quit the server

            LOGGER.debug("we quit the server")
            connectionState.lifecycle = LifecycleState.DISCONNECTED
            eventDispatcher.fire(ConnectionLifecycleEvent(LifecycleState.DISCONNECTED))
        } else {
            // Someone else quit

            for ((name, channel) in channelsState.all) {
                if (channel.users.contains(from)) {
                    channel.users.remove(from)
                }
            }
        }

        LOGGER.trace("someone quit, new states: $connectionState, $channelsState")
    }

}