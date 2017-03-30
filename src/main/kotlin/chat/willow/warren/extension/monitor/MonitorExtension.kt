package chat.willow.warren.extension.monitor

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.extension.monitor.MonitorMessage
import chat.willow.kale.irc.message.extension.monitor.rpl.*
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

class MonitorExtension(initialState: MonitorState, private val kaleRouter: IKaleRouter<IKaleIrcMessageHandler>, private val sink: IMessageSink, private val eventDispatcher: IWarrenEventDispatcher) : ICapExtension, IStateCapturing<MonitorState> {

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
        kaleRouter.register(RplMonOnline.command, onlineHandler)
        kaleRouter.register(RplMonOffline.command, offlineHandler)
        kaleRouter.register(RplMonList.command, listHandler)
        kaleRouter.register(RplEndOfMonList.command, endOfListHandler)
        kaleRouter.register(RplMonListIsFull.command, listFullHandler)

        if (internalState.users.isEmpty()) {
            return
        }

        // TODO: Message splitting
        sink.write(MonitorMessage.Add.Command(internalState.users))
    }

    override fun tearDown() {
        kaleRouter.unregister(RplMonOnline.command)
        kaleRouter.unregister(RplMonOffline.command)
        kaleRouter.unregister(RplMonList.command)
        kaleRouter.unregister(RplEndOfMonList.command)
        kaleRouter.unregister(RplMonListIsFull.command)
    }

}