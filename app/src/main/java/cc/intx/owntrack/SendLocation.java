package cc.intx.owntrack;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;

import java.security.cert.Certificate;

/* This class is a wrapper for OtServer to abstract communication with the remote server */

public class SendLocation {
    private String TAG;

    private Context context;

    //Save the url, which is saved in the user settings
    private String standardUrl = "";
    //Save the commonSecret
    private String commonSecret = "";
    //Save the pinnedCertificateHash
    private String pinnedCertificate = "";
    //Save if the user allowed self signed certificates
    private boolean allowSelfsigned = false;

    //Save the last error, to not confuse errors with return values
    private int lastError = 0;

    //Return the last error and reset it
    public int getLastError() {
        int returnError = lastError;

        //Reset error
        lastError = 0;

        return returnError;
    }

    //Change the commonSecret, called initially and whenever settings are changed
    public void changeCommonSecret(String commonSecret) {
        this.commonSecret = commonSecret;
    }

    //Change the pinned certificate, called initially and whenever settings are changed
    public void changePinnedCertificate(String pinnedCertificate) {
        this.pinnedCertificate = pinnedCertificate;
    }

    //Change the setting whether to allow self signing or not, called initially and whenever settings are changed
    public void changeSelfsigned(boolean allowSelfsigned) {
        this.allowSelfsigned = allowSelfsigned;
    }

    //Change the standard url to use if not stated otherwise, called initially and whenever settings are changed
    public void changeUrl(String url) {
        this.standardUrl = url;
    }

    //Constructor
    public SendLocation(String TAG, Context context) {
        this.TAG = TAG;

        this.context = context;
    }

    //Try to connect to a custom URL. Does not check any other settings
    public int pingNewServer(String sUrl) {
        OtServer server = new OtServer(TAG, sUrl, allowSelfsigned);

        if (server.getError() != 0) {
            server.disconnect();
            lastError = server.getError();
            return server.getError();
        }

        server.checkConnection();
        int result = server.getError();
        server.disconnect();

        return result;
    }

    //Check the user configuration and the configuration of the remote server
    public int checkServerSettings() {
        OtServer server = new OtServer(TAG, standardUrl, allowSelfsigned);
        int ern = server.getError();
        if (ern != 0) {
            server.disconnect();
            return ern;
        }

        int result = server.checkSettings(commonSecret, pinnedCertificate);
        server.disconnect();

        return result;
    }

    //Get the cert of a custom URL
    public Certificate[] getCerts(String sUrl) {
        OtServer server = new OtServer(TAG, sUrl, allowSelfsigned);
        int ern = server.getError();
        if (ern != 0) {
            server.disconnect();
            lastError = ern;
            return null;
        }

        Certificate[] returnCerts = server.getCerts();
        ern = server.getError();
        if (ern != 0) {
            server.disconnect();
            lastError = ern;
            return null;
        }

        server.disconnect();

        return returnCerts;
    }

    //Upload the location list
    public int upload(JSONArray locationList) {
        lastError = 0;

        OtServer server = new OtServer(TAG, standardUrl, allowSelfsigned);
        int ern = server.getError();
        if (ern != 0) {
            server.disconnect();
            lastError = ern;
            return 0;
        }

        int result = server.upload(commonSecret, pinnedCertificate, locationList);
        lastError = server.getError();
        server.disconnect();

        return result;
    }
}
