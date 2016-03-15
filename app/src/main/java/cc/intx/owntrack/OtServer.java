package cc.intx.owntrack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OtServer {
    private String TAG;

    private int error = 0;

    private HttpsURLConnection httpsURLConnection;
    private InputStream inputStream;

    private boolean allowSelfsigned;

    private TrustManager[] allowSelfsignedTrustManager;
    private void createSelfSignedTrustManager() {
        allowSelfsignedTrustManager = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };
    }

    public OtServer(String TAG, String sUrl, boolean allowSelfsigned) {
        this.TAG = TAG;
        this.allowSelfsigned = allowSelfsigned;

        SSLContext sslContext = null;
        if (allowSelfsigned) {
            createSelfSignedTrustManager();

            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, allowSelfsignedTrustManager, new SecureRandom());
            } catch (Exception e) {
                e.printStackTrace();
                error = -1;
                return;
            }
        }

        URL url;
        try {
            url = new URL(sUrl);
        } catch (Exception e) {
            e.printStackTrace();

            error = 2; //Bad url
            return;
        }

        try {
            httpsURLConnection = (HttpsURLConnection) url.openConnection();

            if (allowSelfsigned) {
                httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            }
        } catch (IOException e) {
            e.printStackTrace();

            error = -1;
            return;
        }

        if (httpsURLConnection == null) {

            error = -1;
            return;
        }
    }

    public void checkConnection() {
        if (httpsURLConnection != null) {
            try {
                inputStream = new BufferedInputStream(httpsURLConnection.getInputStream());
                error = 0;
            } catch (IOException e) {
                if (e instanceof UnknownHostException) {
                    error = 4;
                } else if (e instanceof SSLHandshakeException) {
                    error = 6;
                } else {
                    e.printStackTrace();
                    error = -1;
                }
            }
        } else {
            error = -1;
        }
    }

    public Certificate[] getCerts() {
        checkConnection();
        try {
            return httpsURLConnection.getServerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getError() {
        int returnError = error;

        //Reset error
        error = 0;

        return returnError;
    }

    public void disconnect() {
        if (httpsURLConnection != null) {
            httpsURLConnection.disconnect();
        }
    }
}
