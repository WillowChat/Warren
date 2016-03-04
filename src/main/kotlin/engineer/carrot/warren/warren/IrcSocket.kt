package engineer.carrot.warren.warren

import engineer.carrot.warren.warren.irc.IMessageSink
import engineer.carrot.warren.warren.irc.IMessageSource
import engineer.carrot.warren.warren.irc.message.IIrcMessageParser
import engineer.carrot.warren.warren.irc.message.IIrcMessageSerialiser
import engineer.carrot.warren.warren.irc.message.IrcMessage
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class IrcSocket(val server: String, val port: Int, val parser: IIrcMessageParser, val serialiser: IIrcMessageSerialiser): IMessageSource, IMessageSink {
    lateinit var socket: Socket
    lateinit var source: BufferedSource
    lateinit var sink: BufferedSink

    fun setUp(): Boolean {
        socket = try {
            SSLSocketFactory.getDefault().createSocket(server, port)
        } catch (exception: Exception) {
            println("failed to connect using: $server:$port")

            return false
        }

        sink = Okio.buffer(Okio.sink(socket.outputStream))
        source = Okio.buffer(Okio.source(socket.inputStream))

        source.timeout().timeout(5, TimeUnit.SECONDS)

        return true
    }

    fun tearDown() {
        socket.close()
    }

    override fun nextMessage(): Pair<String, IrcMessage?>? {
        val line = try {
            source.readUtf8LineStrict()
        } catch (exception: IOException) {
            return null
        }

        println(">> $line")

        val message = parser.parse(line)
        return Pair(line, message)
    }

    override fun writeMessage(message: IrcMessage) {
        val line = serialiser.serialise(message)
        if (line == null) {
            println("failed to serialise message: $message")
        }

        println("<< $line")

        sink.writeString(line + "\r\n", Charsets.UTF_8)
        sink.flush()
    }
}