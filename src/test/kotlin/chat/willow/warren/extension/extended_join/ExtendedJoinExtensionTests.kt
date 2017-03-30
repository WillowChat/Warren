package chat.willow.warren.extension.extended_join

import chat.willow.kale.IKale
import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.helper.CaseMapping
import chat.willow.kale.irc.message.rfc1459.JoinMessage
import chat.willow.warren.state.*
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test

class ExtendedJoinExtensionTests {

    private lateinit var sut: ExtendedJoinExtension
    private lateinit var mockKale: IKale
    private lateinit var mockKaleRouter: IKaleRouter<IKaleIrcMessageHandler>
    private lateinit var mockJoinHandler: IKaleMessageHandler<JoinMessage.Message>
    private lateinit var mockJoinIrcMessageHandler: IKaleIrcMessageHandler
    private lateinit var channelsState: ChannelsState
    private lateinit var connectionState: ConnectionState

    @Before fun setUp() {
        val lifecycleState = LifecycleState.DISCONNECTED

        mockKale = mock()
        mockKaleRouter = mock()
        mockJoinHandler = mock()
        mockJoinIrcMessageHandler = mock()
        whenever(mockKaleRouter.handlerFor("JOIN")).thenReturn(mockJoinIrcMessageHandler)

        mockJoinHandler = mock()
        channelsState = emptyChannelsState(CaseMappingState(CaseMapping.RFC1459))
        connectionState = ConnectionState(server = "test.server", port = 6697, nickname = "test-nick", user = "test-nick", lifecycle = lifecycleState)

        sut = ExtendedJoinExtension(mockKaleRouter, channelsState, CaseMappingState(CaseMapping.RFC1459))
    }

    @Test fun test_setUp_RegistersCorrectHandlers() {
        sut.setUp()

        verify(mockKaleRouter).register(eq("JOIN"), any<ExtendedJoinHandler>())
    }

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.setUp()
        sut.tearDown()

        inOrder(mockKaleRouter) {
            verify(mockKaleRouter).unregister("JOIN")
            verify(mockKaleRouter).register("JOIN", mockJoinIrcMessageHandler)
        }
    }

}