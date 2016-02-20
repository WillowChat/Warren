package engineer.carrot.warren.warren

import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import java.net.Socket
import java.util.concurrent.TimeUnit

class SocketLineSourceSink(val server: String, val port: Int): ILineSourceSink {
    lateinit var socket: Socket
    lateinit var source: BufferedSource
    lateinit var sink: BufferedSink

    override fun setUp(): Boolean {
        socket = try {
            Socket(server, port)
        } catch (exception: Exception) {
            println("failed to connect using: $server:$port")

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

    override fun readLine(): String? {
        val line = source.readUtf8LineStrict()
        println(">> $line")
        return line
    }

    override fun writeLine(line: String) {
        println("<< $line")
        sink.writeString(line + "\r\n", Charsets.UTF_8)
        sink.flush()
    }
}