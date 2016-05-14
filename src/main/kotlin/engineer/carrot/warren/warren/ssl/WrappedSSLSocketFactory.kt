package engineer.carrot.warren.warren.ssl

import engineer.carrot.warren.warren.loggerFor
import javax.net.ssl.*
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.ArrayList

class WrappedSSLSocketFactory(val fingerprints: Set<String>?) : SSLSocketFactory() {
    val LOGGER = loggerFor<WrappedSSLSocketFactory>()

    private var factory: SSLSocketFactory

    init {
        val defaultFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

        if (fingerprints == null) {
            this.factory = defaultFactory
        } else if (fingerprints.isEmpty()) {
            val customFactory = createDangerZoneSocketFactory()
            if (customFactory != null) {
                this.factory = customFactory
            } else {
                this.factory = defaultFactory
            }
        } else {
            val customFactory = createFingerprintsSocketFactory(fingerprints)
            if (customFactory != null) {
                this.factory = customFactory
            } else {
                this.factory = defaultFactory
            }
        }
    }

    private fun createDangerZoneSocketFactory(): SSLSocketFactory? {
        val tm = arrayOf<TrustManager>(DangerZoneTrustAllX509TrustManager())

        val context: SSLContext
        try {
            context = SSLContext.getInstance("TLS")
        } catch (e: NoSuchAlgorithmException) {
            LOGGER.error("failed to create danger zone SSL Context for WrappedSSLSocketFactory: {}", e)

            return null
        }

        try {
            context.init(arrayOfNulls<KeyManager>(0), tm, SecureRandom())
        } catch (e: KeyManagementException) {
            LOGGER.error("failed to initialise danger zone custom SSL Context for WrappedSSLSocketFactory: {}", e)

            return null
        }

        return context.socketFactory
    }

    private fun createFingerprintsSocketFactory(fingerprints: Set<String>): SSLSocketFactory? {
        val tm = arrayOf<TrustManager>(SHA256SignaturesX509TrustManager(fingerprints))

        val context: SSLContext
        try {
            context = SSLContext.getInstance("TLS")
        } catch (e: NoSuchAlgorithmException) {
            LOGGER.error("failed to create fingerprints SSL Context for WrappedSSLSocketFactory: {}", e)

            return null
        }

        try {
            context.init(arrayOfNulls<KeyManager>(0), tm, SecureRandom())
        } catch (e: KeyManagementException) {
            LOGGER.error("failed to initialise fingerprints custom SSL Context for WrappedSSLSocketFactory: {}", e)

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
