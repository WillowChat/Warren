package chat.willow.warren.extension.monitor

import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.IStateCapturing

data class MonitorState(var maxCount: Int)

class MonitorExtension(initialState: MonitorState) : ICapExtension, IStateCapturing<MonitorState> {

    private val LOGGER = loggerFor<MonitorExtension>()

    internal var internalState: MonitorState = initialState
    @Volatile override var state: MonitorState = initialState.copy()

    override fun captureStateSnapshot() {
        state = internalState.copy()
    }

    override fun setUp() {
        LOGGER.info("set up monitor extension")
    }

    override fun tearDown() {
        LOGGER.info("tore down monitor extension")
    }

}