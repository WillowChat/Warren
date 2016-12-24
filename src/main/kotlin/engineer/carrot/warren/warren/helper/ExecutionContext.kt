package engineer.carrot.warren.warren.helper

import kotlin.concurrent.thread

interface IExecutionContext {

    fun execute(block: () -> Unit)
    fun tearDown() = Unit

}

typealias SimpleBlock = () -> Unit

class ThreadedExecutionContext(private val name: String): IExecutionContext {

    private val LOGGER = loggerFor<ThreadedExecutionContext>()
    private var currentThread: Thread? = null

    override fun execute(block: SimpleBlock) {
        if (currentThread != null) {
            throw RuntimeException("tried to execute block without tearing down first")
        }

        val threadedBlock = thread(start = false, name = name) {
            block()
        }

        threadedBlock.start()
    }

    override fun tearDown() {
        val thread = currentThread ?: return

        if (thread.isAlive) {
            LOGGER.debug("interrupting $name")
            thread.interrupt()

            currentThread = null
        }
    }

}

class ImmediateExecutionContext: IExecutionContext {

    override fun execute(block: () -> Unit) {
        block()
    }

}

class NoOpExecutionContext: IExecutionContext {

    override fun execute(block: () -> Unit) {
        // NO-OP
    }

}