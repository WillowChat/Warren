package chat.willow.warren.extension.extended_join

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import chat.willow.kale.IKale
import chat.willow.kale.IKaleHandler
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.kale.irc.message.utility.CaseMapping
import chat.willow.warren.state.*
import org.junit.Before
import org.junit.Test

class ExtendedJoinExtensionTests {

    private lateinit var sut: ExtendedJoinExtension
    private lateinit var mockKale: IKale
    private lateinit var mockJoinHandler: IKaleHandler<JoinMessage>
    private lateinit var channelsState: ChannelsState
    private lateinit var connectionState: ConnectionState

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED

        mockKale = mock()
        mockJoinHandler = mock()
        whenever(mockKale.handlerFor(JoinMessage::class.java)).thenReturn(mockJoinHandler)

        mockJoinHandler = mock()
        channelsState = emptyChannelsState(CaseMappingState(CaseMapping.RFC1459))
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)

        sut = ExtendedJoinExtension(mockKale, channelsState, CaseMappingState(CaseMapping.RFC1459))
    }

    @Test fun test_setUp_RegistersCorrectHandlers() {
        sut.setUp()

        verify(mockKale).register(any<ExtendedJoinHandler>())
    }

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.setUp()
        sut.tearDown()

        verify(mockKale).unregister(any<ExtendedJoinHandler>())
    }

}