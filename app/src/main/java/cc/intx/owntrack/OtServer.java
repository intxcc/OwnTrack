package cc.intx.owntrack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

public class OtServer {
    private int error = 0;

    private HttpsURLConnection httpsURLConnection;
    private InputStream inputStream;

    public OtServer(String sUrl) {
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
            } catch (IOException e) {
                if (e instanceof UnknownHostException) {

                    error = 4; //Can't connect
                } else {
                    e.printStackTrace();

                    error = -1;
                }
            }
        } else {
            error = -1;
        }
    }

    public int getError() {
        return error;
    }

    public void disconnect() {
        if (httpsURLConnection != null) {
            httpsURLConnection.disconnect();
        }
    }
}
