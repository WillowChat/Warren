package engineer.carrot.warren.warren.ssl

import com.google.common.io.BaseEncoding

import javax.net.ssl.X509TrustManager
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

internal class FingerprintX509TrustManager(private val forciblyAcceptedSHA1Fingerprints: Set<String>) : X509TrustManager {

    @Throws(CertificateException::class)
    override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {
        throw CertificateException("Forcible Trust Manager is not made to verify client certificates")
    }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {
        var sha1Digest = try {
            MessageDigest.getInstance("SHA1")
        } catch (e: NoSuchAlgorithmException) {
            println("Couldn't get SHA1 instance: $e")

            throw CertificateException("Couldn't check server trust because SHA1 digest instance is missing!")
        }

        var allTrusted = true

        println("Checking presented certificates against forcibly trusted: ")
        for (certificate in x509Certificates) {
            certificate.checkValidity()

            sha1Digest.update(certificate.encoded)
            val sha1DigestString = BaseEncoding.base16().encode(sha1Digest.digest())
            if (this.forciblyAcceptedSHA1Fingerprints.contains(sha1DigestString)) {
                println(" $sha1DigestString IS trusted")
            } else {
                println(" $sha1DigestString IS NOT trusted")
                allTrusted = false
            }
        }

        if (!allTrusted) {
            throw CertificateException("Certificates were presented with SHA1 signatures that we DO NOT trust!")
        } else {
            println("All presented certificates were forcibly trusted by us")
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}
