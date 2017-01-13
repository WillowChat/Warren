package engineer.carrot.warren.warren.helper

interface IExecutionContext {

    fun execute(block: () -> Unit)
    fun tearDown() = Unit

}

typealias SimpleBlock = () -> Unit

class ThreadedExecutionContext(private val name: String, private val threadFactory: IThreadFactory = ThreadFactory()): IExecutionContext {

    private val LOGGER = loggerFor<ThreadedExecutionContext>()
    private var currentThread: IWarrenThread? = null
    private val threadLock = Any()

    override fun execute(block: SimpleBlock) {
        if (currentThread != null) {
            throw RuntimeException("tried to execute block without tearing down first")
        }

        synchronized(threadLock) {
            val thread = threadFactory.create(block, name)
            thread.start()

            currentThread = thread
        }
    }

    override fun tearDown() {
        val thread = synchronized(threadLock) {
            currentThread
        } ?: return

        if (thread.isAlive) {
            LOGGER.debug("interrupting $name")
            thread.interrupt()

            currentThread = null
        }
    }

}

internal class ImmediateExecutionContext: IExecutionContext {

    override fun execute(block: () -> Unit) {
        block()
    }

}

internal class NoOpExecutionContext: IExecutionContext {

    override fun execute(block: () -> Unit) {
        // NO-OP
    }

}