package cc.intx.owntrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Autostart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String TAG = context.getString(R.string.app_name); //Set debug string to app name
        Log.d(TAG, "Autostart received.");

        Intent serviceIntend = new Intent(context, TrackingService.class);
        context.startService(serviceIntend);
    }
}
