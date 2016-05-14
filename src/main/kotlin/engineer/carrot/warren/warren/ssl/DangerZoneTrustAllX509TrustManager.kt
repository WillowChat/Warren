package engineer.carrot.warren.warren.ssl

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.net.ssl.X509TrustManager
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

internal class DangerZoneTrustAllX509TrustManager() : X509TrustManager {
    private val LOGGER = LoggerFactory.getLogger(DangerZoneTrustAllX509TrustManager::class.java)

    @Throws(CertificateException::class)
    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {
        throw CertificateException("Forcible Trust Manager is not made to verify client certificates")
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {
        LOGGER.warn("DANGER ZONE: forcefully trusting all presented X509 certificates. This is not secure.")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}
