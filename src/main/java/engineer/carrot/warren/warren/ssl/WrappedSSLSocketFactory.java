package engineer.carrot.warren.warren.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WrappedSSLSocketFactory extends SSLSocketFactory {
    final Logger LOGGER = LoggerFactory.getLogger(WrappedSSLSocketFactory.class);

    private SSLSocketFactory factory;

    public WrappedSSLSocketFactory() {
        this.factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    public WrappedSSLSocketFactory forciblyAcceptCertificatesWithSHA1Fingerprints(Set<String> forciblyAcceptedFingerprintSHA1s) {
        // TODO: check well-formedness of SHA1 strings
        //  Should be: upper case, no punctuation

        SSLSocketFactory socketFactory = this.createFingerprintsAcceptingSocketFactory(forciblyAcceptedFingerprintSHA1s);
        if (socketFactory != null) {
            this.factory = socketFactory;
        }

        return this;
    }

    @Nullable
    private SSLSocketFactory createFingerprintsAcceptingSocketFactory(Set<String> forciblyAcceptedFingerprintSHA1s) {
        TrustManager[] tm = new TrustManager[]{new FingerprintX509TrustManager(forciblyAcceptedFingerprintSHA1s)};

        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Failed to create custom SSL Context for CustomSSLSocketFactory: {}", e);

            return null;
        }

        try {
            context.init(new KeyManager[0], tm, new SecureRandom());
        } catch (KeyManagementException e) {
            LOGGER.error("Failed to initialise custom SSL Context for CustomSSLSocketFactory: {}", e);

            return null;
        }

        return context.getSocketFactory();
    }

    public SSLSocket disableDHEKeyExchange(Socket socket) {
        SSLSocket sslSocket = (SSLSocket) socket;

        List<String> cipherSubset = new ArrayList<>();
        for (String cipherSuites : sslSocket.getEnabledCipherSuites()) {
            if (!cipherSuites.contains("_DHE_")) {
                cipherSubset.add(cipherSuites);
            }
        }

        sslSocket.setEnabledCipherSuites(cipherSubset.toArray(new String[cipherSubset.size()]));

        return sslSocket;
    }

    // SSLSocketFactory

    @Override
    public String[] getDefaultCipherSuites() {
        return this.factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.factory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return this.factory.createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return this.factory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
        return this.factory.createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(InetAddress localAddress, int localPort) throws IOException {
        return this.factory.createSocket(localAddress, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.factory.createSocket(address, port, localAddress, localPort);
    }
}
