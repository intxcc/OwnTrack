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

/* This class is used to communicate with the server.
*  IMPORTANT: Use any instance for just one connection type. Because there are some implementional bugs
*  which throw errors otherwise for multiple connects, but MAINLY because it is initialized with the
*  settings and therefor should be used for a small timeframe, to respect important user settings, like
*  selfsigning, as we are not able to react to changed settings here. */

/* TODO TODO TODO TODO do this shit in threads, this is bad and you know it */

public class OtServer {
    private String TAG;

    private int error = 0;

    //The connection object
    private HttpsURLConnection httpsURLConnection;
    //Almost every function of this class creates an input stream, even if only to ping, so we save it, to reduce multiple input streams
    private InputStream inputStream;

    //Safe if the user has allowed self signing for this connection
    private boolean allowSelfsigned;

    //An "empty" TrustManager which allows any certificate. Use with care (and certificate pinning)
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

    //Initialize connection
    public OtServer(String TAG, String sUrl, boolean allowSelfsigned) {
        this.TAG = TAG;
        this.allowSelfsigned = allowSelfsigned;

        //If the user allowed self signing set the TrustManager to allow all certificates without any errors thrown
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

        //Load url and throw error on bad url
        URL url;
        try {
            url = new URL(sUrl);
        } catch (Exception e) {
            e.printStackTrace();

            error = 2; //Bad url
            return;
        }

        /* Open the connection, this does (MAYBE TODO?) prepare the connection and not actually open it.
           Sounds weir to me as well, because, well, openConnection() but if the host does not exist this error
           is not throws here, but only if we get(Input|Output)Stream
         */
        //TODO what if we have no internet
        try {
            httpsURLConnection = (HttpsURLConnection) url.openConnection();

            if (allowSelfsigned) {
                //Set the "allow all"-TrustManager if self signed is active
                httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            }
        } catch (IOException e) {
            e.printStackTrace();

            error = -1;
            return;
        }

        //Unexpected error handling (It should probably be IMPOSSIBLE to be null here)
        if (httpsURLConnection == null) {
            error = -1;
        }
    }

    //Check connection for errors (Unexpected, Cert bad signature, Host can't be resolved)
    public void checkConnection() {
        if (httpsURLConnection != null) {
            try {
                //Open a stream, to check the connection
                inputStream = new BufferedInputStream(httpsURLConnection.getInputStream());
                error = 0;
            } catch (IOException e) {
                //Check the type of error and set the lastError based on the Exception
                if (e instanceof UnknownHostException) {
                    error = 4;
                } else if (e instanceof SSLHandshakeException) {
                    //TODO This does not check exactely specify a wrong cert, but is close enough. An unsupported Algorithm could probably throw the same error
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

    /* The idea of the server-client authentication is to prove to the server, that we know the common secret
       without actually sending the secret and a NOT reusable identification string for better security.

       So we send a bundle of three variables. The last (c) one is just an indicator if we check a working OwnTrack
       installation. The first (a) is a random String with length 32 used as a salt, the way salts are used to save
       user passwords. The salt is send in cleartext. The second string (b) is the salt appended to the commonSecret
       and then hashed. This makes it impossible to reverse engineer the commonSecret.

       The server on the other hand, has the commonSecret itself and gets the cleartext salt. It combines both
       the same way we do here and checks if both hashes are equal. To make it impossible to intercept a single
       authentication bundle and use that to spam the server with data, the server saves all used salts, to make
       them one-time keys.

       This all might seem like overthinking, but is helpful to prevent spam attacks on the server. Probably one
       could as well just send the common secret because https is supported only, but this way is cooler, already
       implemented and could help in case of an attempt from interstellar intelligent life trying to kill you */
    private JSONObject createAuthenticationBundle(String commonSecret, boolean isPing) {
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

    //Check the current settings and if the remote server is an OwnTrack installation with the same commonSecret
    public int checkSettings(String commonSecret, String pinnedCertificate) {
        JSONObject requestBundle = new JSONObject();

        //Create authentication JSONObject, ping flag is set
        JSONObject authenticationBundle = createAuthenticationBundle(commonSecret, true);
        if (authenticationBundle == null) {
            error = -1;
            return error;
        }

        //The final request has two fields, (a) is authentication and (b) anything else
        try {
            requestBundle.put("a", authenticationBundle);
        } catch (Exception e) {
            e.printStackTrace();
            error = -1;
            return error;
        }

        //Send the request to the remote server
        String responseString = send(requestBundle, pinnedCertificate);

        if (responseString.equals("BADCERT")) {
            error = 9;
            return error;
        }

        //Is the answer an Integer (in String format)? If not something went wrong
        int response;
        try {
            response = Integer.parseInt(responseString);
        } catch (NumberFormatException e) {
            error = 8;
            return error;
        }

        return response;
    }

    //Checks in the certificate used in connection has the same SHA-256 hash as the correctCertificate
    private boolean checkCert(HttpsURLConnection connection, String correctCertificate) {
        String certificate;
        try {
            //Get first cert. TODO No support for multiple certs
            certificate = Misc.getCertFingerprint(connection.getServerCertificates()[0]);
        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
            return false;
        }

        //Return true only if both hashes are not empty and are equal, to prevent errors
        return (correctCertificate.equals(certificate) && !correctCertificate.equals("") && !certificate.equals(""));
    }

    //Upload a JSONArray payload
    public int upload(String commonSecret, String pinnedCertificate, JSONArray locationList) {
        JSONObject requestBundle = new JSONObject();

        //Reset error
        error = 0;

        //Create authentication JSONObject, ping flag is not set
        JSONObject authenticationBundle = createAuthenticationBundle(commonSecret, false);
        if (authenticationBundle == null) {
            error = -1;
        }

        //Create the request (a) is authentication
        try {
            requestBundle.put("a", authenticationBundle);
        } catch (Exception e) {
            e.printStackTrace();
            error = -1;
        }

        //Create the request (b) is payload
        try {
            requestBundle.put("b", locationList);
        } catch (Exception e) {
            e.printStackTrace();
            error = -1;
        }

        //Send the request
        String responseString = send(requestBundle, pinnedCertificate);

        //Check bad cert
        if (responseString.equals("BADCERT")) {
            error = 9;
        }

        int response = 0;
        try {
            response = Integer.parseInt(responseString);
        } catch (NumberFormatException e) {
            error = 8;
        }

        //Return the server response, callee should also check lastError()
        return response;
    }

    //Prepare and execute post request
    private String send(JSONObject requestBundle, String pinnedCertificate) {
        //Convert request to string and then to byte array
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
            //Set output
            httpsURLConnection.setDoOutput(true);

            //Prevent redirect tampering
            httpsURLConnection.setInstanceFollowRedirects(false);

            //Prepare post headers
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpsURLConnection.setRequestProperty("charset", "utf-8");
            httpsURLConnection.setRequestProperty("Content-Length", Integer.toString(requestLength));

            //Disable cache
            httpsURLConnection.setUseCaches(false);

            //Open output stream
            DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());


            //Check certificate against the pinnedCertificate, if there is any
            if (!checkCert(httpsURLConnection, pinnedCertificate) && !pinnedCertificate.equals("")) {
                return "BADCERT";
            }

            //Send the request body
            dataOutputStream.write(requestData);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        //Get the server response
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

        //Return the response string
        return response.toString();
    }

    //Get certificate with initialized settings
    public Certificate[] getCerts() {
        error = 0;

        //Check if the connection is fine
        checkConnection();

        //Abort if there is a connection error
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

    //Returns the last error and resets it
    public int getError() {
        int returnError = error;

        //Reset error
        error = 0;

        return returnError;
    }

    //Disconnect connection. Should always be executed to "free" the connection
    public void disconnect() {
        if (httpsURLConnection != null) {
            httpsURLConnection.disconnect();
        }
    }
}
