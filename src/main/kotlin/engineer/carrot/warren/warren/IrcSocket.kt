package engineer.carrot.warren.warren

import engineer.carrot.warren.kale.IKale
import engineer.carrot.warren.kale.irc.message.IIrcMessageSerialiser
import engineer.carrot.warren.kale.irc.message.IMessage
import engineer.carrot.warren.warren.ssl.WrappedSSLSocketFactory
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.io.IOException
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

class IrcSocket(val server: String, val port: Int, val kale: IKale, val serialiser: IIrcMessageSerialiser) : IMessageSink, IMessageProcessor {
    lateinit var socket: Socket
    lateinit var source: BufferedSource
    lateinit var sink: BufferedSink

    fun setUp(): Boolean {
        val socketFactory = WrappedSSLSocketFactory

        socket = try {
            val socket = socketFactory.createSocket(server, port)
            socketFactory.disableDHEKeyExchange(socket)
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

    override fun process(): Boolean {
        val line = try {
            source.readUtf8LineStrict()
        } catch (exception: IOException) {
            return false
        }

        println(">> $line")

        kale.process(line)
        return true
    }

    override fun <T : IMessage> write(message: T) {
        val ircMessage = kale.serialise(message)
        if (ircMessage == null) {
            println("failed to serialise to irc message: $message")
            return
        }

        val line = serialiser.serialise(ircMessage)
        if (line == null) {
            println("failed to serialise to line: $ircMessage")
            return
        }

        println("<< $line")

        sink.writeString(line + "\r\n", Charsets.UTF_8)
        sink.flush()
    }
}