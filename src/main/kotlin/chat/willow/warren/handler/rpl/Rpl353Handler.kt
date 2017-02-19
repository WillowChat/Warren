package chat.willow.warren.handler.rpl

import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.rpl.Rpl353Message
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.prefix.PrefixParser
import chat.willow.kale.irc.prefix.PrefixSerialiser
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoinedChannelsState
import chat.willow.warren.state.UserPrefixesState
import chat.willow.warren.state.generateUser

class Rpl353Handler(val channelsState: JoinedChannelsState, val userPrefixesState: UserPrefixesState, val caseMappingState: CaseMappingState) : IKaleHandler<Rpl353Message> {

    private val LOGGER = loggerFor<Rpl353Handler>()

    override val messageType = Rpl353Message::class.java

    override fun handle(message: Rpl353Message, tags: ITagStore) {
        val names = message.names

        val channel = channelsState[message.channel]
        if (channel == null) {
            LOGGER.warn("got a 353 for a channel we don't think we're in - bailing: ${message.channel}")
            return
        }

        for (name in names) {
            val (prefixes, userhost) = trimPrefixes(name)

            if (userhost == null || userhost.nick.isEmpty()) {
                LOGGER.warn("nick was empty after trimming: $name")
                continue
            }

            var modes = setOf<Char>()
            prefixes.asSequence()
                    .mapNotNull { userPrefixesState.prefixesToModes[it] }
                    .forEach { modes += it }

            channel.users += generateUser(userhost.nick, modes)
        }

        LOGGER.trace("channel state after 353: $channel")
    }


    private fun trimPrefixes(rawNick: String): Pair<Set<Char>, Prefix?> {
        var nick = rawNick
        var prefixes = setOf<Char>()

        for (char in nick) {
            if (userPrefixesState.prefixesToModes.keys.contains(char)) {
                prefixes += char
                nick = nick.substring(1)
            } else {
                val userhost = PrefixParser.parse(nick)
                return Pair(prefixes, userhost)
            }
        }

        val userhost = PrefixParser.parse(nick)
        return Pair(prefixes, userhost)
    }

}