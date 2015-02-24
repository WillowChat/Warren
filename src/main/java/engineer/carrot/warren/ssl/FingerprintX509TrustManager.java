package engineer.carrot.warren.ssl;

import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

public class FingerprintX509TrustManager implements X509TrustManager {
    final Logger LOGGER = LoggerFactory.getLogger(FingerprintX509TrustManager.class);

    public Set<String> forciblyAcceptedSHA1Fingerprints;

    public FingerprintX509TrustManager(Set<String> forciblyAcceptedSHA1Fingerprints) {
        this.forciblyAcceptedSHA1Fingerprints = forciblyAcceptedSHA1Fingerprints;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        throw new CertificateException("Forcible Trust Manager is not made to verify client certificates");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        MessageDigest sha1Digest = null;
        try {
            sha1Digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Couldn't get SHA1 instance: {}", e);

            throw new CertificateException("Couldn't check server trust because SHA1 digest instance is missing!");
        }

        boolean allTrusted = true;

        LOGGER.info("Checking presented certificates against forcibly trusted: ");
        for (X509Certificate certificate : x509Certificates) {
            certificate.checkValidity();

            sha1Digest.update(certificate.getEncoded());
            String sha1DigestString = BaseEncoding.base16().encode(sha1Digest.digest());
            if (this.forciblyAcceptedSHA1Fingerprints.contains(sha1DigestString)) {
                LOGGER.info(" {} IS trusted", sha1DigestString);
            } else {
                LOGGER.info(" {} IS NOT trusted", sha1DigestString);
                allTrusted = false;
            }
        }

        if (!allTrusted) {
            throw new CertificateException("Certificates were presented with SHA1 signatures that we DO NOT trust!");
        } else {
            LOGGER.info("All presented certificates were forcibly trusted by us");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
