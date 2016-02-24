package cc.intx.owntrack;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private String TAG;

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = getString(R.string.app_name);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //LinearLayout layoutActive = (LinearLayout) findViewById(R.id.linearLayout);
        //LinearLayout layoutActive_overlay = (LinearLayout) findViewById(R.id.linearLayout_overlay);
        /*layoutActive_overlay.setLayoutParams(layoutActive.getLayoutParams());
        layoutActive_overlay.invalidate();*/

        Switch activeSwitch = (Switch) findViewById(R.id.active_switch);
        activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //LinearLayout layoutActive = (LinearLayout) findViewById(R.id.linearLayout);
                    //Log.i(TAG, layoutActive.getAnimation().toString());
                    //layoutActive.get

                    /*Rect ss = new Rect();
                    ss.set(0,0,10,10);
                    //layoutActive.setBac
                    ObjectAnimator anim = ObjectAnimator.ofInt(drawable, "XOffset", 0, 50);
                    anim.setDuration(1000);
                    anim.start();*/

                    Log.i(TAG, "a");
                } else {
                    Log.i(TAG, "l");
                }
            }
        });
    }

    private void startTrackingService() {
        Log.d(TAG, "Start Service");

        /*if (isServiceRunning(TrackingService.class)) {

        }*/
    }
}
