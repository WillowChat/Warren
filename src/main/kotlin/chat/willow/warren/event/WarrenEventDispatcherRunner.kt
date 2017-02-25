package chat.willow.warren.event

import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.state.LifecycleState
import chat.willow.warren.state.emptyChannel
import chat.willow.warren.state.generateUser

object WarrenEventDispatcherRunner {
    @JvmStatic fun main(args: Array<String>) {
        val eventDispatcher = WarrenEventDispatcher()

        eventDispatcher.onAnything {
            println("anything listener: $it")
        }

        eventDispatcher.on(ChannelMessageEvent::class) {
            println("channel message listener 1: $it")
        }

        eventDispatcher.on(PrivateMessageEvent::class) {
            println("private message listener 2: $it")
        }

        eventDispatcher.fire(ChannelMessageEvent(user = generateUser("someone"), channel = emptyChannel("#channel"), message = "something", metadata = TagStore()))
        eventDispatcher.fire(PrivateMessageEvent(user = Prefix(nick = "someone"), message = "something", metadata = TagStore()))
        eventDispatcher.fire(ConnectionLifecycleEvent(lifecycle = LifecycleState.CONNECTED))
    }
}