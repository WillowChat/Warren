package chat.willow.warren.event

import chat.willow.kale.irc.message.rfc1459.ModeMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.ITagStore
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.WarrenChannel
import chat.willow.warren.WarrenChannelUser
import chat.willow.warren.state.LifecycleState

interface IWarrenEvent

typealias IMetadataStore = ITagStore
typealias MetadataStore = TagStore

data class ChannelMessageEvent(val user: WarrenChannelUser, val channel: WarrenChannel, val message: String, val metadata: IMetadataStore = MetadataStore()) : IWarrenEvent
data class ChannelActionEvent(val user: WarrenChannelUser, val channel: WarrenChannel, val message: String, val metadata: IMetadataStore = MetadataStore()) : IWarrenEvent
data class ChannelModeEvent(val user: Prefix?, val channel: WarrenChannel, val modifier: ModeMessage.ModeModifier, val metadata: IMetadataStore = MetadataStore()) : IWarrenEvent
data class UserModeEvent(val user: String, val modifier: ModeMessage.ModeModifier, val metadata: IMetadataStore = MetadataStore()) : IWarrenEvent
data class PrivateMessageEvent(val user: Prefix, val message: String, val metadata: IMetadataStore = MetadataStore()) : IWarrenEvent
data class PrivateActionEvent(val user: Prefix, val message: String, val metadata: IMetadataStore = MetadataStore()) : IWarrenEvent
data class InvitedToChannelEvent(val source: Prefix, val channel: String, val metadata: IMetadataStore = MetadataStore()) : IWarrenEvent

data class RawIncomingLineEvent(val line: String) : IWarrenEvent
data class ConnectionLifecycleEvent(val lifecycle: LifecycleState) : IWarrenEvent