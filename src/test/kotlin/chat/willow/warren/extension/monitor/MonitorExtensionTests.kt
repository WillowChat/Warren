package chat.willow.warren.extension.monitor

import chat.willow.kale.IKale
import chat.willow.warren.extension.monitor.handler.*
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test

class MonitorExtensionTests {

    private lateinit var sut: MonitorExtension
    private lateinit var mockKale: IKale

    @Before fun setUp() {
        val initialState = MonitorState(maxCount = 100)

        mockKale = mock()

        sut = MonitorExtension(initialState, mockKale)
    }

    @Test fun test_setUp_RegistersHandlers() {
        sut.setUp()

        verify(mockKale).register(any<MonitorOnlineHandler>())
        verify(mockKale).register(any<MonitorOfflineHandler>())
        verify(mockKale).register(any<MonitorListHandler>())
        verify(mockKale).register(any<MonitorEndOfListHandler>())
        verify(mockKale).register(any<MonitorListFullHandler>())
    }

    @Test fun test_tearDown_UnregistersHandlers() {
        sut.tearDown()

        verify(mockKale).unregister(any<MonitorOnlineHandler>())
        verify(mockKale).unregister(any<MonitorOfflineHandler>())
        verify(mockKale).unregister(any<MonitorListHandler>())
        verify(mockKale).unregister(any<MonitorEndOfListHandler>())
        verify(mockKale).unregister(any<MonitorListFullHandler>())
    }
}