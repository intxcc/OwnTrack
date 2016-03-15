package cc.intx.owntrack;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
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

    public JSONObject createAuthenticationBundle(String commonSecret, boolean isPing) {
        String authenticationSalt = (new BigInteger(130, new SecureRandom())).toString(32);
        String authenticationHashString = Misc.hash("SHA-256", (commonSecret + authenticationSalt).getBytes());

        JSONObject request = new JSONObject();
        try {
            request.put("a", authenticationSalt);
            request.put("b", authenticationHashString);
            request.put("c", isPing);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return request;
    }

    public int checkSettings(String commonSecret, String pinnedCertificate) {
        JSONObject requestBundle = new JSONObject();

        JSONObject authenticationBundle = createAuthenticationBundle(commonSecret, true);
        if (authenticationBundle == null) {
            return -1;
        }

        try {
            requestBundle.put("a", authenticationBundle);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }


        String responseString = send(requestBundle, pinnedCertificate);

        if (responseString.equals("BADCERT")) {
            return 9;
        }

        int response;
        try {
            response = Integer.parseInt(responseString);
        } catch (NumberFormatException e) {
            return 8;
        }

        return response;
    }

    private boolean checkCert(HttpsURLConnection connection, String correctCertificate) {
        String certificate;
        try {
            certificate = Misc.getCertFingerprint(connection.getServerCertificates()[0]);
        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
            return false;
        }

        return (correctCertificate.equals(certificate) && !correctCertificate.equals("") && !certificate.equals(""));
    }

    public int upload(String commonSecret, String pinnedCertificate, JSONArray locationList) {
        JSONObject requestBundle = new JSONObject();

        error = 0;

        JSONObject authenticationBundle = createAuthenticationBundle(commonSecret, false);
        if (authenticationBundle == null) {
            error = -1;
        }

        try {
            requestBundle.put("a", authenticationBundle);
        } catch (Exception e) {
            e.printStackTrace();
            error = -1;
        }

        try {
            requestBundle.put("b", locationList);
        } catch (Exception e) {
            e.printStackTrace();
            error = -1;
        }

        String responseString = send(requestBundle, pinnedCertificate);

        if (responseString.equals("BADCERT")) {
            error = 9;
        }

        int response = 0;
        try {
            response = Integer.parseInt(responseString);
        } catch (NumberFormatException e) {
            error = 8;
        }

        return response;
    }

    private String send(JSONObject requestBundle, String pinnedCertificate) {
        byte[] requestData;
        int requestLength;
        try {
            requestData = requestBundle.toString().getBytes();
            requestLength = requestData.length;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        try {
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setInstanceFollowRedirects(false);
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpsURLConnection.setRequestProperty("charset", "utf-8");
            httpsURLConnection.setRequestProperty("Content-Length", Integer.toString(requestLength));
            httpsURLConnection.setUseCaches(false);
            DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());

            if (!checkCert(httpsURLConnection, pinnedCertificate) && !pinnedCertificate.equals("")) {
                return "BADCERT";
            }

            dataOutputStream.write(requestData);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        StringBuilder response = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            String bufferString;
            while ((bufferString = br.readLine()) != null) {
                response.append(bufferString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return response.toString();
    }

    public Certificate[] getCerts() {
        checkConnection();

        if (error != 0) {
            return null;
        }

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
