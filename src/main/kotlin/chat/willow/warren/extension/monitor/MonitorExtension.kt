package chat.willow.warren.extension.monitor

import chat.willow.kale.IKale
import chat.willow.kale.irc.message.extension.monitor.MonitorAddMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.warren.IMessageSink
import chat.willow.warren.event.IWarrenEvent
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.cap.ICapExtension
import chat.willow.warren.extension.monitor.handler.*
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.state.IStateCapturing

data class MonitorState(var maxCount: Int, var users: List<String> = listOf())

data class UserOnlineEvent(val prefix: Prefix) : IWarrenEvent
data class UserOfflineEvent(val prefix: Prefix) : IWarrenEvent

class MonitorExtension(initialState: MonitorState, private val kale: IKale, private val sink: IMessageSink, private val eventDispatcher: IWarrenEventDispatcher) : ICapExtension, IStateCapturing<MonitorState> {

    private val LOGGER = loggerFor<MonitorExtension>()

    internal var internalState: MonitorState = initialState
    @Volatile override var state: MonitorState = initialState.copy()

    private val onlineHandler: MonitorOnlineHandler by lazy { MonitorOnlineHandler(eventDispatcher) }
    private val offlineHandler: MonitorOfflineHandler by lazy { MonitorOfflineHandler(eventDispatcher) }
    private val listHandler: MonitorListHandler by lazy { MonitorListHandler() }
    private val endOfListHandler: MonitorEndOfListHandler by lazy { MonitorEndOfListHandler() }
    private val listFullHandler: MonitorListFullHandler by lazy { MonitorListFullHandler() }

    override fun captureStateSnapshot() {
        state = internalState.copy()
    }

    override fun setUp() {
        kale.register(onlineHandler)
        kale.register(offlineHandler)
        kale.register(listHandler)
        kale.register(endOfListHandler)
        kale.register(listFullHandler)

        if (internalState.users.isEmpty()) {
            return
        }

        // TODO: Message splitting
        sink.write(MonitorAddMessage(internalState.users))
    }

    override fun tearDown() {
        kale.unregister(onlineHandler)
        kale.unregister(offlineHandler)
        kale.unregister(listHandler)
        kale.unregister(endOfListHandler)
        kale.unregister(listFullHandler)
    }

}