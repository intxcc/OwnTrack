package cc.intx.owntrack;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

public class SendLocation {
    private String TAG;

    private Context context;

    public SendLocation(String TAG, Context context) {
        this.TAG = TAG;

        this.context = context;
    }

    public int pingNewServer(String sUrl) {
        HttpsURLConnection httpsURLConnection = null;

        try {
            URL url;
            try {
                url = new URL(sUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return 2; //Bad url
            }

            try {
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }

            if (httpsURLConnection == null) {
                return -1;
            }

            try {
                InputStream in = new BufferedInputStream(httpsURLConnection.getInputStream());
            } catch (IOException e) {
                if (e instanceof UnknownHostException) {
                    return 4;
                } else {
                    e.printStackTrace();
                    return -1;
                }
            }

            return 0; //All good
        } finally {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }
        }
    }
}
