package engineer.carrot.warren.warren

class WarrenConnection(val connectionInformation: WarrenConnectionInformation) {
    init {
        print("connection information: $connectionInformation")
    }
}