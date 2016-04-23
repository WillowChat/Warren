package engineer.carrot.warren.warren.handler

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.warren.state.*

class JoinHandler(val connectionState: ConnectionState, val channelsState: ChannelsState) : IKaleHandler<JoinMessage> {
    override val messageType = JoinMessage::class.java

    override fun handle(message: JoinMessage) {
        val channelNames = message.channels
        val source = message.source

        if (source == null) {
            println("got a JOIN but the source was null - not doing anything with it")
            return
        }

        val nick = source.nick

        if (nick == connectionState.nickname) {
            // Us joining a channel

            println("we joined channels: ${message.channels}")

            for (channelName in channelNames) {
                if (!channelsState.joined.containsKey(channelName)) {
                    println("adding $channelName to joined channels with 0 users")

                    channelsState.joined[channelName] = ChannelState(channelName, users = generateUsers())
                } else {
                    println("we're already in $channelName - not adding it again")
                }
            }
        } else {
            // Someone else joined a channel

            for (channelName in channelNames) {
                val channelState = channelsState.joined[channelName]
                if (channelState != null) {
                    channelState.users += generateUser(nick)

                    println("new channel state: ${channelState}")
                } else {
                    println("we were given a JOIN for a channel we aren't in - not doing anything with it: $channelName")
                }
            }
        }
    }

}