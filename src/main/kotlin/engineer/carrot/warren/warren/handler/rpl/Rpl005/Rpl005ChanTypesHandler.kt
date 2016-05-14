package engineer.carrot.warren.warren.handler.rpl.Rpl005

import engineer.carrot.warren.warren.loggerFor
import engineer.carrot.warren.warren.state.ChannelTypesState

interface IRpl005ChanTypesHandler {

    fun handle(rawValue: String, state: ChannelTypesState): Boolean

}

object Rpl005ChanTypesHandler : IRpl005ChanTypesHandler {
    private val LOGGER = loggerFor<Rpl005ChanTypesHandler>()

    override fun handle(rawValue: String, state: ChannelTypesState): Boolean {
        // CHANTYPES: &#

        var value = rawValue

        if (value.isNullOrEmpty()) {
            LOGGER.warn("CHANTYPES value null or empty, bailing")
            return false
        }

        val types = mutableSetOf<Char>()

        for (char in value) {
            types += char
        }

        state.types = types

        LOGGER.debug("handled 005 CHANTYPES: $state")

        return true
    }

}