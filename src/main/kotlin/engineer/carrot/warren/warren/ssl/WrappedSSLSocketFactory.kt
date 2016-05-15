package engineer.carrot.warren.warren.ssl

import engineer.carrot.warren.warren.loggerFor
import javax.net.ssl.*
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class WrappedSSLSocketFactory(private val fingerprints: Set<String>?) {
    val LOGGER = loggerFor<WrappedSSLSocketFactory>()

    private fun createDangerZoneSocketFactory(): SSLSocketFactory {
        val tm = arrayOf<TrustManager>(DangerZoneTrustAllX509TrustManager())

        val context: SSLContext
        try {
            context = SSLContext.getInstance("TLS")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("failed to create danger zone SSL Context for WrappedSSLSocketFactory: {}", e)
        }

        try {
            context.init(arrayOfNulls<KeyManager>(0), tm, SecureRandom())
        } catch (e: KeyManagementException) {
            throw RuntimeException("failed to initialise danger zone custom SSL Context for WrappedSSLSocketFactory: {}", e)
        }

        return context.socketFactory
    }

    private fun createFingerprintsSocketFactory(fingerprints: Set<String>): SSLSocketFactory {
        val tm = arrayOf<TrustManager>(SHA256SignaturesX509TrustManager(fingerprints))

        val context: SSLContext
        try {
            context = SSLContext.getInstance("TLS")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("failed to create fingerprints SSL Context for WrappedSSLSocketFactory: {}", e)
        }

        try {
            context.init(arrayOfNulls<KeyManager>(0), tm, SecureRandom())
        } catch (e: KeyManagementException) {
            throw RuntimeException("failed to initialise fingerprints custom SSL Context for WrappedSSLSocketFactory: {}", e)
        }

        return context.socketFactory
    }

    fun createDefaultFactory(sslParameters: SSLParameters, context: SSLContext): SSLSocketFactory {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val keyStore: KeyStore? = null
        trustManagerFactory.init(keyStore)

        context.init(arrayOfNulls<KeyManager>(0), trustManagerFactory.trustManagers, SecureRandom())

        context.supportedSSLParameters.endpointIdentificationAlgorithm = sslParameters.endpointIdentificationAlgorithm
        context.supportedSSLParameters.cipherSuites = sslParameters.cipherSuites
        context.defaultSSLParameters.endpointIdentificationAlgorithm = sslParameters.endpointIdentificationAlgorithm
        context.defaultSSLParameters.cipherSuites = sslParameters.cipherSuites

        return context.socketFactory
    }

    fun createSocket(host: String, port: Int): SSLSocket {
        val context = SSLContext.getInstance("TLS")
        val sslParameters = SSLParameters()
        sslParameters.cipherSuites = SSLContext.getDefault().defaultSSLParameters.cipherSuites.filterNot { it.contains("_DHE_") }.toTypedArray()

        val factory: SSLSocketFactory = if (fingerprints == null) {
            createDefaultFactory(sslParameters, context)
        } else if (fingerprints.isEmpty()) {
            createDangerZoneSocketFactory()
        } else {
            createFingerprintsSocketFactory(fingerprints)
        }

        val socket = factory.createSocket(host, port) as SSLSocket
        socket.sslParameters = sslParameters

        return socket
    }

}
