package engineer.carrot.warren.warren

interface IMessageProcessor {
    fun processNextMessage(): Boolean
}