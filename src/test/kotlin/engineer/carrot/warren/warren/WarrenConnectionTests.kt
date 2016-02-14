package engineer.carrot.warren.warren

import org.junit.Test
import org.junit.Assert.*

class WarrenConnectionTests {

    @Test fun test_init_doesNotMutateConnectionInformation() {
        val connectionInformation = WarrenConnectionInformation(server = "testserver", port = "testport", nickname = "testnickname")

        val connection = WarrenConnection(connectionInformation)

        assertEquals(connection.connectionInformation.server, "testserver")
        assertEquals(connection.connectionInformation.port, "testport")
        assertEquals(connection.connectionInformation.nickname, "testnickname")
    }
}