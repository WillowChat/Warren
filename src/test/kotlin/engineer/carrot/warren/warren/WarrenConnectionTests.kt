package engineer.carrot.warren.warren

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.junit.Before

class WarrenConnectionTests {

    lateinit var lineSource: ILineSource
    lateinit var lineSink: ILineSink

    @Before fun setUp() {
        lineSource = mock(ILineSource::class.java)
        lineSink = mock(ILineSink::class.java)
    }

    @Test fun test_init_doesNotMutateConnectionInformation() {
        val connectionInformation = ConnectionInfo(server = "testserver", port = 1234, nickname = "testnickname")

        val connection = IRCConnection(connectionInformation, lineSource, lineSink)

        assertEquals(connection.connectionInfo.server, "testserver")
        assertEquals(connection.connectionInfo.port, 1234)
        assertEquals(connection.connectionInfo.nickname, "testnickname")
    }

    @Test fun test_run_SuccessfulSetUp_SendsNickAndUser() {
        val connectionInformation = ConnectionInfo(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = IRCConnection(connectionInformation, lineSource, lineSink)

        connection.run()

        val inOrderVerify = inOrder(lineSink)
        inOrderVerify.verify(lineSink).writeLine("NICK testnickname")
        inOrderVerify.verify(lineSink).writeLine("USER testnickname 8 * testnickname")
    }

    @Test fun test_run_SuccessfulNickUser_ReadsOneLine() {
        val connectionInformation = ConnectionInfo(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = IRCConnection(connectionInformation, lineSource, lineSink)

        connection.run()

        verify(lineSource, times(1)).readLine()
    }
}