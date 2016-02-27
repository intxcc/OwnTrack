package cc.intx.owntrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class TrackingService extends Service {
    private String TAG; //Debug tag

    @Override
    public void onCreate() {
        TAG = getString(R.string.app_name); //Set debug string to app name

        Log.i(TAG, "Create Service");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; //No binding yet
    }

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (pendingIntent == null || alarmManager == null) {
            Log.d(TAG, "Rewrite alarm");

            pendingIntent = PendingIntent.getService(this, 998566, intent, 0);
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 500, 600, pendingIntent);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        Log.i(TAG, "Destroy Service");
    }
}
