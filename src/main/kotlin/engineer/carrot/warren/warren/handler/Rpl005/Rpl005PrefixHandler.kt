package engineer.carrot.warren.warren.handler.Rpl005

import engineer.carrot.warren.kale.irc.CharacterCodes
import engineer.carrot.warren.warren.state.ParsingState
import engineer.carrot.warren.warren.state.UserPrefixesState

interface IRpl005PrefixHandler {
    fun handle(rawValue: String, state: UserPrefixesState): Boolean
}

object Rpl005PrefixHandler: IRpl005PrefixHandler {

    override fun handle(rawValue: String, state: UserPrefixesState): Boolean {
        // PREFIX: (ov)@+

        var value = rawValue

        if (value.getOrNull(0) != CharacterCodes.LEFT_BRACKET) {
            println("no ( in value, bailing")
            return false
        }

        value = value.substring(1)

        val rightBracketPosition = value.indexOf(CharacterCodes.RIGHT_BRACKET)
        if (rightBracketPosition <= 1 || value.endsWith(CharacterCodes.RIGHT_BRACKET)) {
            println("missing or badly placed ) in value, bailing")
            return false
        }

        val modes = value.substring(0, rightBracketPosition)
        val prefixes = value.substring(rightBracketPosition + 1)

        val modesLength = modes.length
        val prefixesLength = prefixes.length

        if (modesLength == 0 || prefixesLength == 0 || modesLength != prefixesLength) {
            println("mistmatched or zero modes or prefixes, bailing")
            return false
        }

        val prefixesToModes = mutableMapOf<Char, Char>()

        for (i in 0..modesLength - 1) {
            val mode = modes[i]
            val prefix = prefixes[i]

            prefixesToModes[prefix] = mode
        }

        state.prefixesToModes = prefixesToModes

        println("handled 005 PREFIX: ${state.prefixesToModes}")

        return true
    }

}