package engineer.carrot.warren.warren.event.internal

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.warren.ILineSource
import engineer.carrot.warren.warren.IMessageSink
import engineer.carrot.warren.warren.event.IWarrenEventDispatcher
import engineer.carrot.warren.warren.event.RawIncomingLineEvent
import engineer.carrot.warren.warren.loggerFor
import java.util.concurrent.LinkedBlockingQueue

interface IWarrenInternalEvent {
    fun execute()
}

interface IWarrenInternalEventSink {
    fun add(event: IWarrenInternalEvent)

    fun add(closure: () -> Unit)
}

interface IWarrenInternalEventSource {
    fun grab(): IWarrenInternalEvent?
}

interface IWarrenInternalEventQueue : IWarrenInternalEventSource, IWarrenInternalEventSink {
    fun clear()
}

class WarrenInternalEventQueue : IWarrenInternalEventQueue {
    private val queue = LinkedBlockingQueue<IWarrenInternalEvent>(100)

    override fun add(event: IWarrenInternalEvent) {
        queue.add(event)
    }

    override fun add(closure: () -> Unit) {
        add(object : IWarrenInternalEvent {
            override fun execute() {
                closure()
            }
        })
    }

    override fun grab(): IWarrenInternalEvent? {
        try {
            return queue.take()
        } catch (e: InterruptedException) {
            return null
        }
    }

    override fun clear() {
        queue.clear()
    }

}

class NewLineEvent(val line: String, val kale: IKale) : IWarrenInternalEvent {
    override fun execute() {
        kale.process(line)
    }

}

class SendSomethingEvent(val message: Any, val sink: IMessageSink) : IWarrenInternalEvent {
    override fun execute() {
        sink.write(message)
    }

}

interface IWarrenInternalEventGenerator {
    fun run()
}

class NewLineWarrenEventGenerator(val queue: IWarrenInternalEventQueue, val kale: IKale, val lineSource: ILineSource, val fireIncomingLineEvent: Boolean, val warrenEventDispatcher: IWarrenEventDispatcher?): IWarrenInternalEventGenerator {

    private val LOGGER = loggerFor<NewLineWarrenEventGenerator>()

    override fun run() {
        do {
            val line = lineSource.nextLine()
            if (line == null) {
                LOGGER.trace("got null line, bailing out")
                return
            } else {
                LOGGER.trace("added to queue: $line")

                if (fireIncomingLineEvent && warrenEventDispatcher != null) {
                    warrenEventDispatcher.fire(RawIncomingLineEvent(line = line))
                }

                queue.add(NewLineEvent(line, kale))
            }
        } while (true)
    }

}