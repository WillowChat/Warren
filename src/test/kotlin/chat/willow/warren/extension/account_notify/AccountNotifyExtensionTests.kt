package chat.willow.warren.extension.account_notify

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

class AccountNotifyExtensionTests {

    private lateinit var sut: AccountNotifyExtension
    private lateinit var mockKaleRouter: IKaleRouter<IKaleIrcMessageHandler>
    private lateinit var channelsState: JoinedChannelsState

    @Before fun setUp() {
        mockKaleRouter = mock()
        channelsState = JoinedChannelsState(CaseMappingState(CaseMapping.RFC1459))

        sut = AccountNotifyExtension(mockKaleRouter, channelsState)
    }

    @Test fun test_setUp_RegistersCorrectHandlers() {
        sut.setUp()

        verify(mockKaleRouter).register(eq("ACCOUNT"), any<AccountHandler>())
    }

    @Test fun test_tearDown_UnregistersCorrectHandlers() {
        sut.tearDown()

        verify(mockKaleRouter).unregister("ACCOUNT")
    }

}