package chat.willow.warren.handler.rpl.isupport

import chat.willow.kale.irc.CharacterCodes
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.ChannelModesState

interface IRpl005ChanModesHandler {

    fun handle(rawValue: String, state: ChannelModesState): Boolean

}

object Rpl005ChanModesHandler : IRpl005ChanModesHandler {

    private val LOGGER = loggerFor<Rpl005ChanModesHandler>()

    override fun handle(rawValue: String, state: ChannelModesState): Boolean {
        // CHANMODES: eIb,k,l,imnpstSr

        val value = rawValue

        if (value.isNullOrEmpty()) {
            LOGGER.warn("CHANMODES value null or empty, bailing")
            return false
        }

        val modeValues = value.split(delimiters = CharacterCodes.COMMA)
        if (modeValues.size < 4) {
            LOGGER.warn("CHANMODES has less than 4 types, bailing")
            return false
        }

        val typeA = parseModes(modeValues[0])
        val typeB = parseModes(modeValues[1])
        val typeC = parseModes(modeValues[2])
        val typeD = parseModes(modeValues[3])

        state.typeA = typeA
        state.typeB = typeB
        state.typeC = typeC
        state.typeD = typeD

        LOGGER.debug("handled 005 CHANMODES: $state")

        return true
    }

    private fun parseModes(typeValues: String): Set<Char> {
        val parsedModes = (0..typeValues.length - 1)
                .map { typeValues[it] }
                .toSet()

        return parsedModes
    }

}