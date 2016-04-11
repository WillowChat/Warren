package engineer.carrot.warren.warren

import com.nhaarman.mockito_kotlin.*
import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.IKaleHandler
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.kale.irc.message.IrcMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.NickMessage
import engineer.carrot.warren.kale.irc.message.rfc1459.UserMessage
import engineer.carrot.warren.warren.handler.PingHandler
import engineer.carrot.warren.warren.handler.Rpl376Handler
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.MockitoAnnotations

class IrcRunnerTests {
    lateinit var runner: IIrcRunner
    lateinit var connectionInfo: ConnectionInfo

    lateinit var mockKale: MockKale
    lateinit var mockSink: IMessageSink
    lateinit var mockProcessor: IMessageProcessor

    @Before fun setUp() {
        connectionInfo = ConnectionInfo(server = "test.server", port = 6697, nickname = "test-nick")
        mockKale = MockKale

        mockSink = mock()
        mockProcessor = mock()

        runner = IrcRunner(connectionInfo, mockKale, mockSink, mockProcessor)

        MockitoAnnotations.initMocks(this)
    }

    @Test fun test_run_RegistersHandlers() {
        runner.run()

        assertEquals(2, mockKale.spyRegisterHandlers.size)
        assertTrue(mockKale.spyRegisterHandlers[0] is PingHandler)
        assertTrue(mockKale.spyRegisterHandlers[1] is Rpl376Handler)
    }

    @Test fun test_run_SendsRegistrationMessages() {
        runner.run()

        val inOrder = inOrder(mockSink)
        inOrder.verify(mockSink).write(NickMessage(nickname = connectionInfo.nickname))
        inOrder.verify(mockSink).write(UserMessage(username = connectionInfo.nickname, mode = "8", realname = connectionInfo.nickname))
    }

    @Test fun test_run_ProcessesOnce() {
        runner.run()

        verify(mockProcessor).process()
    }

}

object MockKale: IKale {
    var spyRegisterHandlers = mutableListOf<IKaleHandler<*>>()

    override fun <T : IMessage> register(handler: IKaleHandler<T>) {
        spyRegisterHandlers.add(handler)
    }

    override fun <T : IMessage> serialise(message: T): IrcMessage? {
        throw UnsupportedOperationException()
    }

    override fun process(line: String) {
        throw UnsupportedOperationException()
    }

}