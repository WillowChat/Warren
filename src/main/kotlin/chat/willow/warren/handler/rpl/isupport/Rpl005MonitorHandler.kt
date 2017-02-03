package chat.willow.warren.handler.rpl.isupport

import chat.willow.warren.extension.cap.CapKeys
import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.monitor.MonitorState
import chat.willow.warren.helper.loggerFor

interface IRpl005MonitorHandler {

    fun handle(rawValue: String, state: MonitorState): Boolean

}

class Rpl005MonitorHandler(val capManager: ICapManager) : IRpl005MonitorHandler {
    private val LOGGER = loggerFor<Rpl005MonitorHandler>()

    override fun handle(rawValue: String, state: MonitorState): Boolean {
        // Monitor: 100

        if (rawValue.isNullOrEmpty()) {
            LOGGER.warn("Monitor value null or empty, bailing")
            return false
        }

        val count = rawValue.toIntOrNull() ?: return false

        state.maxCount = count
        capManager.capEnabled(CapKeys.MONITOR.key)

        LOGGER.debug("handled 005 Monitor: $state")

        return true
    }

}