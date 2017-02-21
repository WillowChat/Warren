package chat.willow.warren

import chat.willow.warren.ISocketFactory
import chat.willow.warren.IrcSocket
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import chat.willow.kale.IKale
import chat.willow.kale.irc.message.IIrcMessageSerialiser
import chat.willow.kale.irc.message.IrcMessage
import okio.BufferedSink
import okio.BufferedSource
import okio.Timeout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.TimeUnit

class IrcSocketTests {

    private lateinit var sut: IrcSocket

    private lateinit var mockSocketFactory: ISocketFactory
    private lateinit var mockKale: IKale
    private lateinit var mockSerialiser: IIrcMessageSerialiser

    private lateinit var mockSocket: Socket
    private lateinit var mockOutputStream: OutputStream
    private lateinit var mockInputStream: InputStream
    private lateinit var mockSink: BufferedSink
    private lateinit var mockSource: BufferedSource
    private lateinit var mockTimeout: Timeout

    @Before fun setUp() {
        mockSocketFactory = mock()
        mockKale = mock()
        mockSerialiser = mock()

        mockSocket = mock<Socket>()
        mockOutputStream = mock<OutputStream>()
        mockInputStream = mock<InputStream>()
        mockSink = mock<BufferedSink>()
        mockSource = mock<BufferedSource>()
        mockTimeout = mock<Timeout>()

        whenever(mockSocketFactory.create()).thenReturn(mockSocket)
        whenever(mockSocket.outputStream).thenReturn(mockOutputStream)
        whenever(mockSocket.inputStream).thenReturn(mockInputStream)
        whenever(mockSocketFactory.sink(any())).thenReturn(mockSink)
        whenever(mockSocketFactory.source(any())).thenReturn(mockSource)
        whenever(mockSource.timeout()).thenReturn(mockTimeout)

        sut = IrcSocket(mockSocketFactory, mockKale, mockSerialiser)
    }

    @Test fun test_setUp_UsesSocketFactoryToCreateNewSocket() {
        sut.setUp()

        verify(mockSocketFactory).create()
    }

    @Test fun test_setUp_SocketFailedToCreate_ReturnsFalse() {
        whenever(mockSocketFactory.create()).thenReturn(null)

        val result = sut.setUp()

        assertFalse(result)
    }

    @Test fun test_setUp_SocketCreated_MakesNewSinkAndSource() {
        sut.setUp()

        verify(mockSocketFactory).sink(mockSocket.outputStream)
        verify(mockSocketFactory).source(mockSocket.inputStream)
    }

    @Test fun test_setUp_SocketCreated_SetsSourceTimeout() {
        sut.setUp()

        verify(mockTimeout).timeout(5, TimeUnit.SECONDS)
    }

    @Test fun test_tearDown_ClosesSocket() {
        sut.setUp()

        sut.tearDown()

        verify(mockSocket).close()
    }

    @Test fun test_nextLine_ReadsUTF8StrictLineFromSource() {
        whenever(mockSource.readUtf8LineStrict()).thenReturn("")
        sut.setUp()

        sut.nextLine()

        verify(mockSource).readUtf8LineStrict()
    }

    @Test fun test_nextLine_ReadsLine_ReturnsCorrectResult() {
        whenever(mockSource.readUtf8LineStrict()).thenReturn("test line")
        sut.setUp()

        val result = sut.nextLine()

        assertEquals("test line", result)
    }

    @Test fun test_nextLine_IOException_TearsDownAndReturnsNull() {
        whenever(mockSource.readUtf8LineStrict()).then { throw IOException() }
        sut.setUp()

        val result = sut.nextLine()

        assertNull(result)
        verify(mockSocket).close()
    }

    @Test fun test_write_AsksKaleToSerialiseMessage() {
        sut.setUp()
        val message = Any()

        sut.write(message)

        verify(mockKale).serialise(message)
    }

    @Test fun test_write_AsksSerialiserToSerialiseMessage() {
        sut.setUp()
        val message = IrcMessage(command = "TEST")
        whenever(mockKale.serialise(any<Any>())).thenReturn(message)

        sut.write(Any())

        verify(mockSerialiser).serialise(message)
    }

    @Test fun test_write_MessageSerialised_WriteStringWithFlush_IncludingNewlines_AsUTF8() {
        sut.setUp()
        val message = "TEST MESSAGE"
        whenever(mockKale.serialise(any<Any>())).thenReturn(IrcMessage(command = ""))
        whenever(mockSerialiser.serialise(any())).thenReturn(message)

        sut.write(Any())

        inOrder(mockSink) {
            verify(mockSink).writeString("TEST MESSAGE\r\n", Charsets.UTF_8)
            verify(mockSink).flush()
        }
    }

    @Test fun test_writeRaw_WritesStringWithFlush_IncludingNewlines_AsUTF8() {
        sut.setUp()

        sut.writeRaw("TEST MESSAGE RAW")

        inOrder(mockSink) {
            verify(mockSink).writeString("TEST MESSAGE RAW\r\n", Charsets.UTF_8)
            verify(mockSink).flush()
        }
    }

}