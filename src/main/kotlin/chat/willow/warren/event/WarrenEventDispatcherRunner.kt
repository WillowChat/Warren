package chat.willow.warren.event

import chat.willow.kale.irc.message.IMessage
import chat.willow.kale.irc.prefix.Prefix
import chat.willow.kale.irc.tag.TagStore
import chat.willow.warren.*
import chat.willow.warren.state.IrcState
import chat.willow.warren.state.LifecycleState
import chat.willow.warren.state.emptyChannel
import chat.willow.warren.state.generateUser

object WarrenEventDispatcherRunner {
    @JvmStatic fun main(args: Array<String>) {
        val eventDispatcher = WarrenEventDispatcher()

        eventDispatcher.onAny {
            println("anything listener: $it")
        }

        eventDispatcher.on(ChannelMessageEvent::class) {
            println("channel message listener 1: $it")
        }

        eventDispatcher.on(PrivateMessageEvent::class) {
            println("private message listener 2: $it")
        }


        val client = DummyMessageSending()
        val channel = WarrenChannel(state = emptyChannel("#channel"), client = client)
        val someone = WarrenChannelUser(state = generateUser("someone"), channel = channel)
        eventDispatcher.fire(ChannelMessageEvent(user = someone, channel = channel, message = "something", metadata = TagStore()))
        eventDispatcher.fire(PrivateMessageEvent(user = Prefix(nick = "someone"), message = "something", metadata = TagStore()))
        eventDispatcher.fire(ConnectionLifecycleEvent(lifecycle = LifecycleState.CONNECTED))
    }
}

private class DummyMessageSending: IClientMessageSending {
    override fun <M : IMessage> send(message: M) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun send(message: String, target: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}