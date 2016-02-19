package engineer.carrot.warren.warren

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.junit.Before

class WarrenConnectionTests {

    lateinit var lineSourceSink: ILineSourceSink

    @Before fun setUp() {
        lineSourceSink = mock(ILineSourceSink::class.java)
    }

    @Test fun test_init_doesNotMutateConnectionInformation() {
        val connectionInformation = WarrenConnectionInformation(server = "testserver", port = 1234, nickname = "testnickname")

        val connection = WarrenConnection(connectionInformation, lineSourceSink)

        assertEquals(connection.connectionInformation.server, "testserver")
        assertEquals(connection.connectionInformation.port, 1234)
        assertEquals(connection.connectionInformation.nickname, "testnickname")
    }

    @Test fun test_connect_SetsUpLineSourceSink() {
        val connectionInformation = WarrenConnectionInformation(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = WarrenConnection(connectionInformation, lineSourceSink)

        `when`(lineSourceSink.setUp()).thenReturn(true)

        connection.connect()

        verify(lineSourceSink).setUp()
    }

    @Test fun test_connect_SuccessfulSetUp_SendsNickAndUser() {
        val connectionInformation = WarrenConnectionInformation(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = WarrenConnection(connectionInformation, lineSourceSink)

        `when`(lineSourceSink.setUp()).thenReturn(true)

        connection.connect()

        val inOrderVerify = inOrder(lineSourceSink)
        inOrderVerify.verify(lineSourceSink).writeLine("NICK testnickname")
        inOrderVerify.verify(lineSourceSink).writeLine("USER testnickname 8 * testnickname")
    }

    @Test fun test_connect_SuccessfulSetUp_TearsDown() {
        val connectionInformation = WarrenConnectionInformation(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = WarrenConnection(connectionInformation, lineSourceSink)

        `when`(lineSourceSink.setUp()).thenReturn(true)

        connection.connect()

        verify(lineSourceSink, times(1)).tearDown()
    }

    @Test fun test_connect_UnsuccessfulSetUp_DoesNotReadOrWriteAnything() {
        val connectionInformation = WarrenConnectionInformation(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = WarrenConnection(connectionInformation, lineSourceSink)

        `when`(lineSourceSink.setUp()).thenReturn(false)

        connection.connect()

        verify(lineSourceSink, never()).writeLine(anyString())
        verify(lineSourceSink, never()).readLine()
    }

    @Test fun test_connect_SuccessfulNickUser_ReadsOneLine() {
        val connectionInformation = WarrenConnectionInformation(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = WarrenConnection(connectionInformation, lineSourceSink)

        `when`(lineSourceSink.setUp()).thenReturn(true)

        connection.connect()

        verify(lineSourceSink, times(1)).readLine()
    }
}