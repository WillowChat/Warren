package chat.willow.warren.helper

import kotlin.concurrent.thread

interface IThreadFactory {

    fun create(block: SimpleBlock, threadName: String): IWarrenThread

}

interface IWarrenThread {

    fun start()
    fun interrupt()
    val isAlive: Boolean

}

class WarrenThread(private val thread: Thread): IWarrenThread {

    override fun start() {
        thread.start()
    }

    override fun interrupt() {
        thread.interrupt()
    }

    override val isAlive: Boolean
        get() = thread.isAlive

}

class ThreadFactory : IThreadFactory {

    override fun create(block: SimpleBlock, threadName: String): IWarrenThread {
        return WarrenThread(thread(start = false, name = threadName) {
            block()
        })
    }

}