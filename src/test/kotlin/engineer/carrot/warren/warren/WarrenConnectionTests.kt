package engineer.carrot.warren.warren

import engineer.carrot.warren.warren.irc.IMessageSink
import engineer.carrot.warren.warren.irc.IMessageSource
import engineer.carrot.warren.warren.irc.message.IrcMessage
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.junit.Before

class WarrenConnectionTests {

    lateinit var messageSource: IMessageSource
    lateinit var messageSink: IMessageSink

    @Before fun setUp() {
        messageSource = mock(IMessageSource::class.java)
        messageSink = mock(IMessageSink::class.java)
    }

    @Test fun test_init_doesNotMutateConnectionInformation() {
        val connectionInformation = ConnectionInfo(server = "testserver", port = 1234, nickname = "testnickname")

        val connection = IrcRunner(connectionInformation, messageSource, messageSink)

        assertEquals(connection.connectionInfo.server, "testserver")
        assertEquals(connection.connectionInfo.port, 1234)
        assertEquals(connection.connectionInfo.nickname, "testnickname")
    }

    @Test fun test_run_SuccessfulSetUp_SendsNickAndUser() {
        val connectionInformation = ConnectionInfo(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = IrcRunner(connectionInformation, messageSource, messageSink)

        connection.run()

        val inOrderVerify = inOrder(messageSink)
        inOrderVerify.verify(messageSink).writeMessage(IrcMessage(command = "NICK", parameters = listOf("testnickname")))
        inOrderVerify.verify(messageSink).writeMessage(IrcMessage(command = "USER", parameters = listOf("testnickname", "8", "*", "testnickname")))
    }

    @Test fun test_run_SuccessfulNickUser_ReadsOneLine() {
        val connectionInformation = ConnectionInfo(server = "testserver", port = 1234, nickname = "testnickname")
        val connection = IrcRunner(connectionInformation, messageSource, messageSink)

        connection.run()

        verify(messageSource, times(1)).nextMessage()
    }
}