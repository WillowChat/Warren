package engineer.carrot.warren.warren.handler

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.irc.message.rfc1459.JoinMessage
import engineer.carrot.warren.kale.irc.message.rpl.Rpl376Message
import engineer.carrot.warren.warren.IMessageSink
import org.junit.Before
import org.junit.Test

class Rpl376HandlerTests {
    lateinit var handler: Rpl376Handler
    lateinit var mockSink: IMessageSink

    @Before fun setUp() {
        mockSink = mock()
        handler = Rpl376Handler(mockSink, channelsToJoin = listOf("#channel1", "#channel2"))
    }

    @Test fun test_handle_SendsPongWithCorrectToken() {
        handler.handle(Rpl376Message(source = "test.source", target = "test-user", contents = "end of motd"))

        verify(mockSink).write(JoinMessage(channels = listOf("#channel1", "#channel2")))
    }
}