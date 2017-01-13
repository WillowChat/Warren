package engineer.carrot.warren.warren.event

import engineer.carrot.warren.kale.irc.prefix.Prefix
import engineer.carrot.warren.warren.state.*
import kotlin.reflect.KClass

interface IEventListener<in T> {
    fun on(event: T)
}

interface IEventListenersWrapper<T> {
    fun add(listener: (T) -> Unit)

    fun fireToAll(event: T)

    operator fun plusAssign(listener: (T) -> Unit) = add(listener)
}

class EventListenersWrapper<T> : IEventListenersWrapper<T> {
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
    fun <T : IWarrenEvent> fire(event: T)

    fun <T : IWarrenEvent> on(eventClass: KClass<T>, listener: (T) -> Unit)

    fun onAnything(listener: (Any) -> Unit)
}

class WarrenEventDispatcher : IWarrenEventDispatcher {
    private val onAnythingListeners: IEventListenersWrapper<Any>

    private var eventToListenersMap = mutableMapOf<Class<*>, IEventListenersWrapper<*>>()

    init {
        onAnythingListeners = EventListenersWrapper<Any>()
    }

    override fun <T : IWarrenEvent> fire(event: T) {
        onAnythingListeners.fireToAll(event)

        @Suppress("UNCHECKED_CAST")
        val listenersWrapper = eventToListenersMap[event.javaClass] as? IEventListenersWrapper<T>
        listenersWrapper?.fireToAll(event)
    }

    override fun <T : IWarrenEvent> on(eventClass: KClass<T>, listener: (T) -> Unit) {
        val wrapper = eventToListenersMap[eventClass.java] ?: constructAndAddWrapperForEvent(eventClass)

        @Suppress("UNCHECKED_CAST")
        val typedWrapper = wrapper as? IEventListenersWrapper<T> ?: return
        typedWrapper += listener
    }

    override fun onAnything(listener: (Any) -> Unit) {
        onAnythingListeners += listener
    }

    private fun <T : IWarrenEvent> constructAndAddWrapperForEvent(eventClass: KClass<T>): IEventListenersWrapper<T> {
        val newWrapper = EventListenersWrapper<T>()
        eventToListenersMap[eventClass.java] = newWrapper

        return newWrapper
    }
}

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