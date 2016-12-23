package engineer.carrot.warren.warren.ssl

import engineer.carrot.warren.warren.helper.loggerFor
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

internal class DangerZoneTrustAllX509TrustManager() : X509TrustManager {

    private val LOGGER = loggerFor<DangerZoneTrustAllX509TrustManager>()

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
