package chat.willow.warren.event

import engineer.carrot.warren.kale.irc.prefix.Prefix
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

        eventDispatcher.fire(ChannelMessageEvent(user = generateUser("someone"), channel = emptyChannel("#channel"), message = "something"))
        eventDispatcher.fire(PrivateMessageEvent(user = Prefix(nick = "someone"), message = "something"))
        eventDispatcher.fire(ConnectionLifecycleEvent(lifecycle = LifecycleState.CONNECTED))
    }
}