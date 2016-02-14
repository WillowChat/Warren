package engineer.carrot.warren.warren

object WarrenRunner {
    @JvmStatic fun main(args: Array<String>) {
        val connectionInformation = WarrenConnectionInformation(server = args[0], port = args[1], nickname = args[2])
        val connection = WarrenConnection(connectionInformation)
    }
}