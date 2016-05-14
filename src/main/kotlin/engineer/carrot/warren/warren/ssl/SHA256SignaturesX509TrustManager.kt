package engineer.carrot.warren.warren.ssl

import engineer.carrot.warren.warren.loggerFor
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.net.ssl.X509TrustManager
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

internal class SHA256SignaturesX509TrustManager(val fingerprints: Set<String>) : X509TrustManager {
    private val LOGGER = loggerFor<DangerZoneTrustAllX509TrustManager>()

    @Throws(CertificateException::class)
    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {
        throw CertificateException("Forcible Trust Manager is not made to verify client certificates")
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {
        var sha256Digest = try {
            MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            LOGGER.error("Couldn't get SHA256 instance: {}", e)

            throw CertificateException("Couldn't check server trust because SHA256 digest instance is missing!")
        }

        var allTrusted = true
        val trustedCertificates = mutableSetOf<String>()
        val untrustedCertificates = mutableSetOf<String>()

        LOGGER.warn("Checking presented certificates against forcibly trusted: ")
        for (certificate in x509Certificates) {
            certificate.checkValidity()

            sha256Digest.update(certificate.encoded)

            val sha256DigestString = String.format("%064x", BigInteger(1, sha256Digest.digest()));
            if (this.fingerprints.contains(sha256DigestString)) {
                LOGGER.warn(" {} IS trusted", sha256DigestString)
                trustedCertificates.add(sha256DigestString)
            } else {
                LOGGER.warn(" {} IS NOT trusted", sha256DigestString)
                untrustedCertificates.add(sha256DigestString)
                allTrusted = false
            }
        }

        if (!allTrusted) {
            throw CertificateException("Certificates were presented with SHA256 signatures that we DO NOT trust! $untrustedCertificates")
        } else {
            LOGGER.warn("All presented certificates were forcibly trusted by us")
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}
