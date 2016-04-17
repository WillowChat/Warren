package engineer.carrot.warren.warren.handler.Rpl005

import com.google.common.base.Splitter
import com.google.common.collect.Sets
import engineer.carrot.warren.kale.irc.CharacterCodes
import engineer.carrot.warren.warren.state.ChannelModesState
import engineer.carrot.warren.warren.state.UserPrefixesState

interface IRpl005ChanModesHandler {

    fun handle(rawValue: String, state: ChannelModesState): Boolean

}

object Rpl005ChanModesHandler : IRpl005ChanModesHandler {

    override fun handle(rawValue: String, state: ChannelModesState): Boolean {
        // CHANMODES: eIb,k,l,imnpstSr

        var value = rawValue

        if (value.isNullOrEmpty()) {
            println("CHANMODES value null or empty, bailing")
            return false
        }

        val modeValues = Splitter.on(',').split(value).toList()
        if (modeValues.size < 4) {
            println("CHANMODES has less than 4 types, bailing")
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

        println("handled 005 CHANMODES: ${state}")

        return true
    }

    private fun parseModes(typeValues: String): Set<Char> {
        val parsedModes = mutableSetOf<Char>()

        for (i in 0..typeValues.length - 1) {
            val c = typeValues[i]

            parsedModes.add(c)
        }

        return parsedModes
    }

}