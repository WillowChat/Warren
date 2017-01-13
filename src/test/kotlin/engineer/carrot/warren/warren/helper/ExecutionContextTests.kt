package engineer.carrot.warren.warren.helper

import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class ExecutionContextTests {

    @Rule @JvmField val exception = ExpectedException.none()
    private lateinit var sut: ThreadedExecutionContext
    private lateinit var mockThreadFactory: IThreadFactory
    private lateinit var mockThread: IWarrenThread

    @Before fun setUp() {
        mockThread = mock()
        mockThreadFactory = mock<IThreadFactory> {
            on { create(any(), any()) } doReturn mockThread
        }

        sut = ThreadedExecutionContext("test thread", mockThreadFactory)
    }

    @Test fun test_threaded_execute_CreatesThreadWithCorrectName() {
        val block = mock<SimpleBlock>()

        sut.execute(block)

        verify(mockThreadFactory).create(block, "test thread")
    }

    @Test fun test_threaded_execute_StartsCreatedThread() {
        val block = mock<SimpleBlock>()
        sut.execute(block)

        verify(mockThread).start()
    }

    @Test fun test_threaded_executeTwice_WithoutTearingDown_ThrowsRuntimeException() {
        exception.expect(RuntimeException::class.java)
        val block = mock<SimpleBlock>()

        sut.execute(block)
        sut.execute(block)
    }

    @Test fun test_threaded_tearDown_WithoutExecuting_DoesNotInterrupt() {
        sut.tearDown()

        verify(mockThread, never()).interrupt()
    }

    @Test fun test_threaded_tearDown_AfterExecuting_ThreadIsAlive_InterruptsThread() {
        whenever(mockThread.isAlive).thenReturn(true)
        sut.execute(mock<SimpleBlock>())

        sut.tearDown()

        verify(mockThread).interrupt()
    }

    @Test fun test_threaded_tearDown_AfterExecuting_ThreadIsDead_DoesNotInterruptThread() {
        whenever(mockThread.isAlive).thenReturn(false)
        sut.execute(mock<SimpleBlock>())

        sut.tearDown()

        verify(mockThread, never()).interrupt()
    }

    @Test fun test_ImmediateExecutionContext_execute_RunsBlock() {
        val block = mock<SimpleBlock>()
        val context = ImmediateExecutionContext()

        context.execute(block)

        verify(block).invoke()
    }

    @Test fun test_NoOpExecutionContext_execute_DoesNotRunBlock() {
        val block = mock<SimpleBlock>()
        val context = NoOpExecutionContext()

        context.execute(block)

        verify(block, never()).invoke()
    }

}