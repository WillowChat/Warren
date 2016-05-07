package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.LifecycleState
import kotlin.reflect.KClass

data class ChannelMessageEvent(val user: Prefix, val channel: String, val message: String)
data class PrivateMessageEvent(val user: Prefix, val message: String)
data class ConnectionLifecycleEvent(val lifecycle: LifecycleState)

interface IEventListener<T> {
    fun on(event: T)
}

interface IEventListenersWrapper<T> {
    fun add(listener: (T) -> Unit)

    fun fireToAll(event: T)

    operator fun plusAssign(listener: (T) -> Unit) = add(listener)
}

class EventListenersWrapper<T>: IEventListenersWrapper<T> {
    private var listeners: Set<IEventListener<T>> = setOf()

    override fun fireToAll(event: T) {
        listeners.forEach { it.on(event) }
    }

    override fun add(listener: (T) -> Unit) {
        listeners += object : IEventListener<T> {
            override fun on(event: T) = listener(event)
        }
    }
}

interface IWarrenEventDispatcher {
    fun <T: Any> fire(event: T)
}

object WarrenEventDispatcher: IWarrenEventDispatcher {
    val onAnythingListeners: IEventListenersWrapper<Any>
    val onChannelMessageListeners: IEventListenersWrapper<ChannelMessageEvent>
    val onPrivateMessageListeners: IEventListenersWrapper<PrivateMessageEvent>
    val onConnectionLifecycleListeners: IEventListenersWrapper<ConnectionLifecycleEvent>

    private var eventToListenersMap = mutableMapOf<Class<*>, IEventListenersWrapper<*>>()

    init {
        onAnythingListeners = EventListenersWrapper<Any>()
        onChannelMessageListeners = EventListenersWrapper<ChannelMessageEvent>()
        onPrivateMessageListeners = EventListenersWrapper<PrivateMessageEvent>()
        onConnectionLifecycleListeners = EventListenersWrapper<ConnectionLifecycleEvent>()

        mapEventToListeners(ChannelMessageEvent::class, onChannelMessageListeners)
        mapEventToListeners(PrivateMessageEvent::class, onPrivateMessageListeners)
        mapEventToListeners(ConnectionLifecycleEvent::class, onConnectionLifecycleListeners)
    }

    private fun <T : Any> mapEventToListeners(eventType: KClass<T>, listeners: IEventListenersWrapper<T>) {
        mapEventToListenersUsingJavaClassType(eventType.java, listeners)
    }

    private fun <T : Any> mapEventToListenersUsingJavaClassType(eventType: Class<T>, listeners: IEventListenersWrapper<T>) {
        eventToListenersMap[eventType] = listeners
    }

    override fun <T: Any> fire(event: T) {
        onAnythingListeners.fireToAll(event)

        @Suppress("UNCHECKED_CAST")
        val listenersWrapper = eventToListenersMap[event.javaClass] as? IEventListenersWrapper<T>
        listenersWrapper?.fireToAll(event)
    }

    @JvmStatic fun main(args: Array<String>) {
        val eventDispatcher = WarrenEventDispatcher

        eventDispatcher.onAnythingListeners += {
            println("anything listener: $it")
        }

        eventDispatcher.onChannelMessageListeners += {
            println("channel message listener 1: $it")
        }

        eventDispatcher.onChannelMessageListeners += {
            println("channel message listener 2: $it")
        }

        eventDispatcher.onPrivateMessageListeners += {
            println("private message listener 1: $it")
        }

        eventDispatcher.fire(ChannelMessageEvent(user = Prefix(nick = "someone"), channel = "#channel", message = "something"))
        eventDispatcher.fire(PrivateMessageEvent(user = Prefix(nick = "someone"), message = "something"))
        eventDispatcher.fire(ConnectionLifecycleEvent(lifecycle = LifecycleState.CONNECTED))
    }
}