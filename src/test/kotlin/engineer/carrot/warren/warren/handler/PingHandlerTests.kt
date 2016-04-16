package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.PingMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.PongMessage
import engineer.carrot.warren.warren.IMessageSink
import org.junit.Before
import org.junit.Test

class PingHandlerTests {

    lateinit var handler: PingHandler
    lateinit var mockSink: IMessageSink

    @Before fun setUp() {
        mockSink = mock()
        handler = PingHandler(mockSink)
    }

    @Test fun test_handle_SendsPongWithCorrectToken() {
        handler.handle(PingMessage(token = "TestToken"))

        verify(mockSink).write(PongMessage(token = "TestToken"))
    }

}