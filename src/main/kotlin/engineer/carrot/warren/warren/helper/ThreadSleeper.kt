package engineer.carrot.warren.warren.helper

interface ISleeper {

    fun sleep(ms: Long)

}

object ThreadSleeper : ISleeper {

    override fun sleep(ms: Long) {
        Thread.sleep(ms)
    }

}