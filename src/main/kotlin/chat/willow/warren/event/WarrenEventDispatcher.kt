package chat.willow.warren.event

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
    private val onAnythingListeners: IEventListenersWrapper<Any> = EventListenersWrapper<Any>()

    private var eventToListenersMap = mutableMapOf<Class<*>, IEventListenersWrapper<*>>()

    override fun <T : IWarrenEvent> fire(event: T) {
        onAnythingListeners.fireToAll(event)

        @Suppress("UNCHECKED_CAST")
        val listenersWrapper = eventToListenersMap[event::class.java] as? IEventListenersWrapper<T>
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