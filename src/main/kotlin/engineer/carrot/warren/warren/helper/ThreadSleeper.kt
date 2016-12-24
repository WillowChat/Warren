package engineer.carrot.warren.warren.helper

interface ISleeper {

    fun sleep(ms: Long): Boolean

}

object ThreadSleeper : ISleeper {

    override fun sleep(ms: Long): Boolean {
        if (!Thread.currentThread().isInterrupted) {
            try {
                Thread.sleep(ms)
            } catch (exception: InterruptedException) {
                return false
            }

            return true
        }

        return false
    }

}