package cc.intx.owntrack;

import android.content.Context;

public class SendLocation {
    private String TAG;

    private Context context;

    public SendLocation(String TAG, Context context) {
        this.TAG = TAG;

        this.context = context;
    }

    public int pingNewServer(String sUrl) {
        OtServer server = new OtServer(sUrl);

        if (server.getError() != 0) {
            server.disconnect();
            return server.getError();
        }

        server.checkConnection();
        int result = server.getError();
        server.disconnect();

        return result;
    }
}
