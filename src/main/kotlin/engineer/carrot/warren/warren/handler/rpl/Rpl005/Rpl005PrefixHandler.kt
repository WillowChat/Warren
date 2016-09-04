package engineer.carrot.warren.warren.handler.rpl.Rpl005

import engineer.carrot.warren.kale.irc.CharacterCodes
import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.UserPrefixesState

interface IRpl005PrefixHandler {

    fun handle(rawValue: String, state: UserPrefixesState): Boolean

}

object Rpl005PrefixHandler : IRpl005PrefixHandler {

    private val LOGGER = loggerFor<Rpl005PrefixHandler>()

    override fun handle(rawValue: String, state: UserPrefixesState): Boolean {
        // PREFIX: (ov)@+

        var value = rawValue

        if (value.getOrNull(0) != CharacterCodes.LEFT_ROUNDED_BRACKET) {
            LOGGER.warn("no ( in value, bailing")
            return false
        }

        value = value.substring(1)

        val rightBracketPosition = value.indexOf(CharacterCodes.RIGHT_ROUNDED_BRACKET)
        if (rightBracketPosition <= 1 || value.endsWith(CharacterCodes.RIGHT_ROUNDED_BRACKET)) {
            LOGGER.warn("missing or badly placed ) in value, bailing")
            return false
        }

        val modes = value.substring(0, rightBracketPosition)
        val prefixes = value.substring(rightBracketPosition + 1)

        val modesLength = modes.length
        val prefixesLength = prefixes.length

        if (modesLength == 0 || prefixesLength == 0 || modesLength != prefixesLength) {
            LOGGER.warn("mistmatched or zero modes or prefixes, bailing")
            return false
        }

        val prefixesToModes = mutableMapOf<Char, Char>()

        for (i in 0..modesLength - 1) {
            val mode = modes[i]
            val prefix = prefixes[i]

            prefixesToModes[prefix] = mode
        }

        state.prefixesToModes = prefixesToModes

        LOGGER.debug("handled 005 PREFIX: $state")

        return true
    }

}