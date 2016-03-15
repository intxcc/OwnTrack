package cc.intx.owntrack;

import android.content.Context;
import android.util.Log;

import java.security.cert.Certificate;

public class SendLocation {
    private String TAG;

    private Context context;

    private String standardUrl = "";
    private String commonSecret = "";
    private String pinnedCertificate = "";
    private boolean allowSelfsigned = false;

    private int lastError = 0;

    public int getLastError() {
        int returnError = lastError;

        //Reset error
        lastError = 0;

        return returnError;
    }

    public void changeCommonSecret(String commonSecret) {
        this.commonSecret = commonSecret;
    }

    public void changePinnedCertificate(String pinnedCertificate) {
        this.pinnedCertificate = pinnedCertificate;
    }

    public void changeSelfsigned(boolean allowSelfsigned) {
        this.allowSelfsigned = allowSelfsigned;
    }

    public void changeUrl(String url) {
        this.standardUrl = url;
    }

    public SendLocation(String TAG, Context context) {
        this.TAG = TAG;

        this.context = context;
    }

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
}
