package chat.willow.warren.event

import chat.willow.kale.irc.message.rfc1459.ModeMessage
import chat.willow.kale.irc.message.rfc1459.PrivMsgMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.warren.IMessageSink
import chat.willow.warren.event.internal.IWarrenInternalEventQueue
import chat.willow.warren.event.internal.SendSomethingEvent
import chat.willow.warren.state.ChannelState
import chat.willow.warren.state.ChannelUserState
import chat.willow.warren.state.LifecycleState

interface IWarrenEvent

data class ChannelMessageEvent(val user: ChannelUserState, val channel: ChannelState, val message: String) : IWarrenEvent
data class ChannelActionEvent(val user: ChannelUserState, val channel: ChannelState, val message: String) : IWarrenEvent
data class ChannelModeEvent(val user: Prefix?, val channel: ChannelState, val modifier: ModeMessage.ModeModifier) : IWarrenEvent
data class UserModeEvent(val user: String, val modifier: ModeMessage.ModeModifier) : IWarrenEvent
data class PrivateMessageEvent(val user: Prefix, val message: String) : IWarrenEvent
data class PrivateActionEvent(val user: Prefix, val message: String) : IWarrenEvent
data class ConnectionLifecycleEvent(val lifecycle: LifecycleState) : IWarrenEvent
data class RawIncomingLineEvent(val line: String) : IWarrenEvent