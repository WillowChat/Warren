package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.irc.message.IIrcMessageSerialiser
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.warren.ssl.WrappedSSLSocketFactory
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.IOException
import java.io.InterruptedIOException
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

interface ILineSource {
    fun nextLine(): String?
}

class IrcSocket(val server: String, val port: Int, val kale: IKale, val serialiser: IIrcMessageSerialiser) : IMessageSink, ILineSource {
    private val LOGGER = loggerFor<IrcSocket>()

    lateinit var socket: Socket
    lateinit var source: BufferedSource
    lateinit var sink: BufferedSink

    override fun setUp(): Boolean {
        val socketFactory = WrappedSSLSocketFactory

        socket = try {
            val socket = socketFactory.createSocket(server, port)
            socketFactory.disableDHEKeyExchange(socket)
        } catch (exception: Exception) {
            LOGGER.info("failed to connect using: $server:$port")

            return false
        }

        sink = Okio.buffer(Okio.sink(socket.outputStream))
        source = Okio.buffer(Okio.source(socket.inputStream))

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
            LOGGER.warn("exception waiting for line: $exception")
            return null
        } catch (exception: InterruptedIOException) {
            LOGGER.warn("process wait interrupted, bailing out")
            tearDown()
            return null
        }

        LOGGER.trace(">> $line")

        return line
    }

    override fun <T : IMessage> write(message: T) {
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
}