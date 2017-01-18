package chat.willow.warren.handler.rpl

import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.rfc1459.rpl.Rpl353Message
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoinedChannelsState
import chat.willow.warren.state.UserPrefixesState
import chat.willow.warren.state.generateUser

class Rpl353Handler(val channelsState: JoinedChannelsState, val userPrefixesState: UserPrefixesState, val caseMappingState: CaseMappingState) : IKaleHandler<Rpl353Message> {

    private val LOGGER = loggerFor<Rpl353Handler>()

    override val messageType = Rpl353Message::class.java

    override fun handle(message: Rpl353Message, tags: Map<String, String?>) {
        val names = message.names

        val channel = channelsState[message.channel]
        if (channel == null) {
            LOGGER.warn("got a 353 for a channel we don't think we're in - bailing: ${message.channel}")
            return
        }

        for (name in names) {
            val (prefixes, nick) = trimPrefixes(name)

            if (nick.isEmpty()) {
                LOGGER.warn("nick was empty after trimming: $name")
                continue
            }

            var modes = setOf<Char>()
            prefixes.asSequence()
                    .mapNotNull { userPrefixesState.prefixesToModes[it] }
                    .forEach { modes += it }

            channel.users += generateUser(nick, modes)
        }

        LOGGER.trace("channel state after 353: $channel")
    }


    private fun trimPrefixes(rawNick: String): Pair<Set<Char>, String> {
        var nick = rawNick
        var prefixes = setOf<Char>()

        for (char in nick) {
            if (userPrefixesState.prefixesToModes.keys.contains(char)) {
                prefixes += char
                nick = nick.substring(1)
            } else {
                return Pair(prefixes, nick)
            }
        }

        return Pair(prefixes, nick)
    }

}