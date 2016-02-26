package cc.intx.owntrack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

public class TrackingService extends Service {
    private String TAG;

    @Override
    public void onCreate() {
        TAG = getString(R.string.app_name); //Set debug string to app name

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        Log.i(TAG, "Service stopped");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");

        return Service.START_STICKY;
    }
}
