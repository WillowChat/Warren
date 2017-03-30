package chat.willow.warren.extension.chghost

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.helper.CaseMapping
import chat.willow.warren.state.CaseMappingState
import chat.willow.warren.state.JoinedChannelsState
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class ChgHostExtensionTests {

    private lateinit var sut: ChgHostExtension
    private lateinit var mockKaleRouter: IKaleRouter<IKaleIrcMessageHandler>
    private lateinit var channelsState: JoinedChannelsState

    @Before fun setUp() {
        mockKaleRouter = mock()
        channelsState = JoinedChannelsState(CaseMappingState(CaseMapping.RFC1459))

        sut = ChgHostExtension(mockKaleRouter, channelsState)
    }

    @Test fun test_setUp_RegistersCorrectHandlers() {
        sut.setUp()

        verify(mockKaleRouter).register(eq("CHGHOST"), any<ChgHostHandler>())
    }

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.tearDown()

        verify(mockKaleRouter).unregister("CHGHOST")
    }

}