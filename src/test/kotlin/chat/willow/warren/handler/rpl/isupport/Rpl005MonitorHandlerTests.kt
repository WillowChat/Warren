package chat.willow.warren.handler.rpl.isupport

import chat.willow.warren.extension.cap.ICapManager
import chat.willow.warren.extension.monitor.MonitorState
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class Rpl005MonitorHandlerTests {

    private lateinit var sut: Rpl005MonitorHandler
    private lateinit var monitorState: MonitorState
    private lateinit var mockCapManager: ICapManager

    @Before fun setUp() {
        mockCapManager = mock()

        sut = Rpl005MonitorHandler(mockCapManager)

        monitorState = MonitorState(maxCount = 0)

    }

    @Test fun test_WellFormed() {
        sut.handle("123", monitorState)

        assertEquals(MonitorState(maxCount = 123), monitorState)
    }

    @Test fun test_EmptyString_DoesNotModifyState() {
        sut.handle("", monitorState)

        assertEquals(MonitorState(maxCount = 0), monitorState)
    }

    @Test fun test_NonEmptyString_NotAnInt_DoesNotModifyState() {
        sut.handle("abc", monitorState)

        assertEquals(MonitorState(maxCount = 0), monitorState)
    }

}
