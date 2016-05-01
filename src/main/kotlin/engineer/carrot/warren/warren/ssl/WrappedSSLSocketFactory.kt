package engineer.carrot.warren.warren.ssl

import javax.net.ssl.*
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.util.ArrayList

object WrappedSSLSocketFactory : SSLSocketFactory() {

    private var factory: SSLSocketFactory

    init {
        this.factory = SSLSocketFactory.getDefault() as SSLSocketFactory
    }

    fun disableDHEKeyExchange(socket: Socket): SSLSocket {
        val sslSocket = socket as SSLSocket

        val cipherSubset = ArrayList<String>()
        for (cipherSuites in sslSocket.enabledCipherSuites) {
            if (!cipherSuites.contains("_DHE_")) {
                cipherSubset.add(cipherSuites)
            }
        }

        sslSocket.enabledCipherSuites = cipherSubset.toArray<String>(arrayOfNulls<String>(cipherSubset.size))

        return sslSocket
    }

    // SSLSocketFactory

    override fun getDefaultCipherSuites(): Array<String> {
        return this.factory.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return this.factory.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return this.factory.createSocket(socket, host, port, autoClose)
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return this.factory.createSocket(host, port)
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return this.factory.createSocket(host, port, localAddress, localPort)
    }

    @Throws(IOException::class)
    override fun createSocket(localAddress: InetAddress, localPort: Int): Socket {
        return this.factory.createSocket(localAddress, localPort)
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return this.factory.createSocket(address, port, localAddress, localPort)
    }
}
