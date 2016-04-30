package engineer.carrot.warren.warren.ssl

import javax.net.ssl.*
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.ArrayList

object WrappedSSLSocketFactory : SSLSocketFactory() {

    private var factory: SSLSocketFactory

    init {
        this.factory = SSLSocketFactory.getDefault() as SSLSocketFactory
    }

    fun forciblyAcceptCertificatesWithSHA1Fingerprints(forciblyAcceptedFingerprintSHA1s: Set<String>): WrappedSSLSocketFactory {
        // TODO: check well-formedness of SHA1 strings
        //  Should be: upper case, no punctuation

        val socketFactory = this.createFingerprintsAcceptingSocketFactory(forciblyAcceptedFingerprintSHA1s)
        if (socketFactory != null) {
            this.factory = socketFactory
        }

        return this
    }

    private fun createFingerprintsAcceptingSocketFactory(forciblyAcceptedFingerprintSHA1s: Set<String>): SSLSocketFactory? {
        val tm = arrayOf<TrustManager>(FingerprintX509TrustManager(forciblyAcceptedFingerprintSHA1s))

        val context: SSLContext
        try {
            context = SSLContext.getInstance("TLS")
        } catch (e: NoSuchAlgorithmException) {
            println("Failed to create custom SSL Context for CustomSSLSocketFactory: $e")

            return null
        }

        try {
            context.init(arrayOfNulls<KeyManager>(0), tm, SecureRandom())
        } catch (e: KeyManagementException) {
            println("Failed to initialise custom SSL Context for CustomSSLSocketFactory: $e")

            return null
        }

        return context.socketFactory
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
