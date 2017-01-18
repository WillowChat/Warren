package chat.willow.warren

import chat.willow.warren.IMessageSink
import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.irc.message.IIrcMessageSerialiser
import chat.willow.warren.helper.loggerFor
import chat.willow.warren.ssl.WrappedSSLSocketFactory
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.TimeUnit

interface ILineSource {

    fun nextLine(): String?

}

interface ISocketFactory {

    fun create(): Socket?
    fun sink(stream: OutputStream): BufferedSink
    fun source(stream: InputStream): BufferedSource

}

abstract class SocketFactory : ISocketFactory {

    override fun sink(stream: OutputStream): BufferedSink {
        return Okio.buffer(Okio.sink(stream))
    }

    override fun source(stream: InputStream): BufferedSource {
        return Okio.buffer(Okio.source(stream))
    }

}

class PlaintextSocketFactory(val server: String, val port: Int) : SocketFactory() {

    private val LOGGER = loggerFor<PlaintextSocketFactory>()

    override fun create(): Socket? {
        try {
            return Socket(server, port)
        } catch (exception: Exception) {
            LOGGER.error("failed to connect using $server:$port $exception")
        }

        return null
    }

}

class TLSSocketFactory(val server: String, val port: Int, val fingerprints: Set<String>?) : SocketFactory() {

    private val LOGGER = loggerFor<TLSSocketFactory>()

    override fun create(): Socket? {
        val socketFactory = WrappedSSLSocketFactory(fingerprints)

        try {
            val socket = socketFactory.createSocket(server, port)
            socket.startHandshake()

            return socket
        } catch (exception: Exception) {
            LOGGER.error("failed to connect using: $server:$port $exception")
        }

        return null
    }

}

class IrcSocket(val socketFactory: ISocketFactory, val kale: IKale, val serialiser: IIrcMessageSerialiser) : IMessageSink, ILineSource {

    private val LOGGER = loggerFor<IrcSocket>()

    private lateinit var socket: Socket
    private lateinit var source: BufferedSource
    private lateinit var sink: BufferedSink

    override fun setUp(): Boolean {
        val rawSocket = socketFactory.create()

        if (rawSocket == null) {
            LOGGER.error("failed to set up socket, bailing")
            return false
        }

        socket = rawSocket

        sink = socketFactory.sink(socket.outputStream)
        source = socketFactory.source(socket.inputStream)

        source.timeout().timeout(5, TimeUnit.SECONDS)

        return true
    }

    override fun tearDown() {
        socket.close()
    }

    override fun nextLine(): String? {
        val line = try {
            source.readUtf8LineStrict()
        } catch (exception: IOException) {
            LOGGER.warn("some type of IOException, bailing out")
            tearDown()
            return null
        }


        LOGGER.trace(">> $line")

        return line
    }

    override fun write(message: Any) {
        val ircMessage = kale.serialise(message)
        if (ircMessage == null) {
            LOGGER.error("failed to serialise to irc message: $message")
            return
        }

        val line = serialiser.serialise(ircMessage)
        if (line == null) {
            LOGGER.error("failed to serialise to line: $ircMessage")
            return
        }

        LOGGER.trace("<< $line")

        sink.writeString(line + "\r\n", Charsets.UTF_8)
        sink.flush()
    }

    override fun writeRaw(line: String) {
        LOGGER.trace("<< RAW: $line")

        sink.writeString(line + "\r\n", Charsets.UTF_8)
        sink.flush()
    }

}