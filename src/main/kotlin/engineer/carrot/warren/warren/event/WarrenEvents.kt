package engineer.carrot.warren.warren.event

import engineer.carrot.warren.kale.irc.message.rfc1459.ModeMessage
import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.ChannelState
import engineer.carrot.warren.warren.state.ChannelUserState
import engineer.carrot.warren.warren.state.LifecycleState

interface IWarrenEvent

data class ChannelMessageEvent(val user: ChannelUserState, val channel: ChannelState, val message: String) : IWarrenEvent
data class ChannelActionEvent(val user: ChannelUserState, val channel: ChannelState, val message: String) : IWarrenEvent
data class ChannelModeEvent(val user: Prefix?, val channel: ChannelState, val modifier: ModeMessage.ModeModifier) : IWarrenEvent
data class UserModeEvent(val user: String, val modifier: ModeMessage.ModeModifier) : IWarrenEvent
data class PrivateMessageEvent(val user: Prefix, val message: String) : IWarrenEvent
data class PrivateActionEvent(val user: Prefix, val message: String) : IWarrenEvent
data class ConnectionLifecycleEvent(val lifecycle: LifecycleState) : IWarrenEvent
data class RawIncomingLineEvent(val line: String) : IWarrenEvent