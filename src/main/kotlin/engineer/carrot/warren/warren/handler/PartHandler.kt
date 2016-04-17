package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.PartMessage
import engineer.carrot.warren.warren.state.ChannelsState
import engineer.carrot.warren.warren.state.ConnectionState

class PartHandler(val connectionState: ConnectionState, val channelsState: ChannelsState) : IKaleHandler<PartMessage> {
    override val messageType = PartMessage::class.java

    override fun handle(message: PartMessage) {
        val channelNames = message.channels
        val source = message.source

        if (source == null) {
            println("got a PART but the source was null - not doing anything with it")
            return
        }

        val nick = source.nick

        if (nick == connectionState.nickname) {
            // Us parting a channel

            println("we parted channels: ${message.channels}")

            for (channelName in channelNames) {
                if (channelsState.joined.containsKey(channelName)) {
                    println("removing $channelName from joined channels")

                    channelsState.joined.remove(channelName)
                } else {
                    println("we already left $channelName - not leaving it again")
                }
            }
        } else {
            // Someone else parted a channel

            for (channelName in channelNames) {
                val channelState = channelsState.joined[channelName]
                if (channelState != null) {
                    channelState.users.remove(nick)

                    println("new channel state: ${channelState}")
                } else {
                    println("we were given a PART for a user who we don't think is in the channel - not doing anything with it: $channelName")
                }
            }
        }
    }

}