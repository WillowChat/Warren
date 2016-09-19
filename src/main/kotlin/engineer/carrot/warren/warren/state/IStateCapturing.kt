package engineer.carrot.warren.warren.state

interface IStateCapturing<out T> {

    val state: T
    fun captureStateSnapshot()

}