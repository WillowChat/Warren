package chat.willow.warren.ssl

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class SHA256SignatureX509TrustManagerTests {

    @Rule @JvmField val exception = ExpectedException.none()
    private lateinit var sut: SHA256SignaturesX509TrustManager
    private lateinit var fingerprints: Set<String>

    @Before fun setUp() {
        fingerprints = setOf()
        sut = SHA256SignaturesX509TrustManager(fingerprints)
    }

    @Test fun test_checkClientTrusted_ThrowsCertificateException() {
        exception.expect(CertificateException::class.java)

        sut.checkClientTrusted(emptyArray(), "")
    }

    @Test fun test_checkServerTrusted_NoCertificates_DoesNotThrowException() {
        sut.checkServerTrusted(emptyArray(), "")
    }

    @Test fun test_checkServerTrusted_SingleTrustedCertificate_DoesNotThrowException() {
        // SHA256 encoding of 0x00 0x01 0x02
        fingerprints = setOf("ae4b3280e56e2faf83f414a6e3dabe9d5fbe18976544c05fed121accb85b53fc")
        sut = SHA256SignaturesX509TrustManager(fingerprints)

        val certificate: X509Certificate = mock()
        whenever(certificate.encoded).thenReturn(byteArrayOf(0x00, 0x01, 0x02))

        sut.checkServerTrusted(arrayOf(certificate), "")
    }

    @Test fun test_checkServerTrusted_MultipleTrustedCertificates_DoesNotThrowException() {
        // SHA256 encoding of 0x00 0x01 0x02, 0x03 0x04 0x05
        fingerprints = setOf("ae4b3280e56e2faf83f414a6e3dabe9d5fbe18976544c05fed121accb85b53fc", "2848698aa4b3431e3db06c343ca2cb0455f8aaf16c85cdd828c92ddf7dc134f8")
        sut = SHA256SignaturesX509TrustManager(fingerprints)

        val certificateOne: X509Certificate = mock()
        val certificateTwo: X509Certificate = mock()
        whenever(certificateOne.encoded).thenReturn(byteArrayOf(0x00, 0x01, 0x02))
        whenever(certificateTwo.encoded).thenReturn(byteArrayOf(0x03, 0x04, 0x05))

        sut.checkServerTrusted(arrayOf(certificateOne, certificateTwo), "")
    }

    @Test fun test_checkServerTrusted_SingleUnrustedCertificate_ThrowsCertificateException() {
        fingerprints = setOf("12345")
        sut = SHA256SignaturesX509TrustManager(fingerprints)

        val certificate: X509Certificate = mock()
        whenever(certificate.encoded).thenReturn(byteArrayOf(0x00, 0x01, 0x02))

        exception.expect(CertificateException::class.java)

        sut.checkServerTrusted(arrayOf(certificate), "")
    }

    @Test fun test_checkServerTrusted_MixedCertificateTrusts_ThrowsCertificateException() {
        // SHA256 encoding of fake, 0x00 0x01 0x02
        fingerprints = setOf("12345", "ae4b3280e56e2faf83f414a6e3dabe9d5fbe18976544c05fed121accb85b53fc")
        sut = SHA256SignaturesX509TrustManager(fingerprints)

        val certificateOne: X509Certificate = mock()
        val certificateTwo: X509Certificate = mock()
        whenever(certificateOne.encoded).thenReturn(byteArrayOf(0x00, 0x01, 0x02))
        whenever(certificateTwo.encoded).thenReturn(byteArrayOf(0x03, 0x04, 0x05))

        exception.expect(CertificateException::class.java)

        sut.checkServerTrusted(arrayOf(certificateOne, certificateTwo), "")
    }

    @Test fun test_getAcceptedIssuers_IsEmpty() {
        assertEquals(0, sut.acceptedIssuers.size)
    }

}


