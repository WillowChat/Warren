package chat.willow.warren.handler.rpl.Rpl005

import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.CaseMappingState

interface IRpl005CaseMappingHandler {

    fun handle(rawValue: String, state: CaseMappingState): Boolean

}

object Rpl005CaseMappingHandler : IRpl005CaseMappingHandler {

    private val LOGGER = loggerFor<Rpl005CaseMappingHandler>()

    override fun handle(rawValue: String, state: CaseMappingState): Boolean {
        // CaseMapping: rfc1459

        if (rawValue.isNullOrEmpty()) {
            LOGGER.warn("CaseMapping value null or empty, bailing")
            return false
        }

        state.mapping = when (rawValue.toLowerCase()) {
            "strict-rfc1459" -> CaseMapping.STRICT_RFC1459
            "ascii" -> CaseMapping.ASCII
            else -> CaseMapping.RFC1459
        }

        LOGGER.debug("handled 005 CaseMapping: $state")

        return true
    }

}