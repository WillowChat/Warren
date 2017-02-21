package chat.willow.warren.ssl

import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.security.cert.CertificateException


class DangerZoneTrustAllX509TrustManagerTests {

    @Rule @JvmField val exception = ExpectedException.none()!!
    private lateinit var sut: DangerZoneTrustAllX509TrustManager

    @Before fun setUp() {
        sut = DangerZoneTrustAllX509TrustManager()
    }

    @Test fun test_checkClientTrusted_ThrowsCertificateException() {
        exception.expect(CertificateException::class.java)

        sut.checkClientTrusted(emptyArray(), "")
    }

    @Test fun test_checkServerTrusted_DoesNotThrowException() {
        sut.checkServerTrusted(emptyArray(), "")
    }

    @Test fun test_getAcceptedIssuers_IsEmpty() {
        assertArrayEquals(emptyArray(), sut.acceptedIssuers)
    }

}