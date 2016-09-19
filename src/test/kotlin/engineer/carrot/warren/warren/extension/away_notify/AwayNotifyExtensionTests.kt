package engineer.carrot.warren.warren.extension.away_notify

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.irc.message.utility.CaseMapping
import engineer.carrot.warren.warren.state.CaseMappingState
import engineer.carrot.warren.warren.state.JoinedChannelsState
import org.junit.Before
import org.junit.Test

class AwayNotifyExtensionTests {

    private lateinit var sut: AwayNotifyExtension
    private lateinit var mockKale: IKale
    private lateinit var channelsState: JoinedChannelsState

    @Before fun setUp() {
        mockKale = mock()
        channelsState = JoinedChannelsState(CaseMappingState(CaseMapping.RFC1459))

        sut = AwayNotifyExtension(mockKale, channelsState)
    }

    @Test fun test_setUp_RegistersCorrectHandlers() {
        sut.setUp()

        verify(mockKale).register(any<AwayHandler>())
    }

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.tearDown()

        verify(mockKale).unregister(any<AwayHandler>())
    }

}