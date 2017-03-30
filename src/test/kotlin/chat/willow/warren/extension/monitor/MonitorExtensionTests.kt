package chat.willow.warren.extension.monitor

import chat.willow.kale.IKaleIrcMessageHandler
import chat.willow.kale.IKaleRouter
import chat.willow.kale.irc.message.IMessage
import chat.willow.kale.irc.message.extension.monitor.MonitorMessage
import chat.willow.kale.irc.message.extension.monitor.rpl.*
import chat.willow.warren.IMessageSink
import chat.willow.warren.event.IWarrenEventDispatcher
import chat.willow.warren.extension.monitor.handler.*
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test

class MonitorExtensionTests {

    private lateinit var sut: MonitorExtension
    private lateinit var mockKaleRouter: IKaleRouter<IKaleIrcMessageHandler>
    private lateinit var mockSink: IMessageSink
    private lateinit var mockEventDispatcher: IWarrenEventDispatcher

    @Before fun setUp() {
        val initialState = MonitorState(maxCount = 100)

        mockKaleRouter = mock()
        mockSink = mock()
        mockEventDispatcher = mock()

        sut = MonitorExtension(initialState, mockKaleRouter, mockSink, mockEventDispatcher)
    }

    @Test fun test_setUp_RegistersHandlers() {
        sut.setUp()

        verify(mockKaleRouter).register(eq(RplMonOnline.command), any<MonitorOnlineHandler>())
        verify(mockKaleRouter).register(eq(RplMonOffline.command), any<MonitorOfflineHandler>())
        verify(mockKaleRouter).register(eq(RplMonList.command), any<MonitorListHandler>())
        verify(mockKaleRouter).register(eq(RplEndOfMonList.command), any<MonitorEndOfListHandler>())
        verify(mockKaleRouter).register(eq(RplMonListIsFull.command), any<MonitorListFullHandler>())
    }

    @Test fun test_tearDown_UnregistersHandlers() {
        sut.tearDown()

        verify(mockKaleRouter).unregister(RplMonOnline.command)
        verify(mockKaleRouter).unregister(RplMonOffline.command)
        verify(mockKaleRouter).unregister(RplMonList.command)
        verify(mockKaleRouter).unregister(RplEndOfMonList.command)
        verify(mockKaleRouter).unregister(RplMonListIsFull.command)
    }

    @Test fun test_setUp_NoUsersToMonitor_SendsNothing() {
        sut.internalState.users = listOf()

        sut.setUp()

        verify(mockSink, never()).write(any<IMessage>())
    }

    @Test fun test_setUp_UsersToMonitor_SendsMonitorAddMessages() {
        sut.internalState.users = listOf("user1", "user2", "user3")

        sut.setUp()

        verify(mockSink).write(MonitorMessage.Add.Command(targets = listOf("user1", "user2", "user3")))
    }

}