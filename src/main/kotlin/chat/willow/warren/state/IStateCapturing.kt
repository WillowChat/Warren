package chat.willow.warren.state

interface IStateCapturing<out T> {

    val state: T

    fun captureStateSnapshot()

}