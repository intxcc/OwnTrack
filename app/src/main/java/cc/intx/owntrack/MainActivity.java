package cc.intx.owntrack;

import android.Manifest;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /* Debug tag */
    public String TAG;

    /* Static variables */
    final static private int animationSpeed = 600;
    final static private int fastAnimationSpeed = 200;
    final static private TimeInterpolator animationInterpolator = new FastOutSlowInInterpolator();

    /* Global objects */
    private FrameLayout switchLayoutOverlay;
    private FrameLayout switchLayoutF;
    private TextView switchTextOverlay;
    private GridView gridview;
    private Switch activeSwitch;

    ServerSettingsClass serverSettingsClass;

    /* Extend control service class, so we can use this class variables more easily */
    public class ServiceControl extends ServiceControlClass {
        public ServiceControl(Context context) {
            /* Copy parent constructor */
            super(context, TAG);
        }

        /* Called if the service status changes (e.g. active -> not active) */
        public void onChangeStatus() {
            /* Implement onstatuschange action */
            onServiceStatusChange();
        }

        /* Initially get all information like server settings and last location */
        public void onBound() {
            serverSettingsClass.loadSavedSettings();
        }
    }
    private ServiceControl serviceControl;

    /* ----------------- */
    /* PRIVATE FUNCTIONS */

    /* Rebuild overlay (e.g. on layout changes) with current state */
    private void switchOverlayRebuild() {switchSetOverlay(isSwitchOverlayActive, false);}
    /*Switch state and animate said change */
    private void switchSetOverlay(boolean toState) {switchSetOverlay(toState, true);}

    /* This signalizes only the overlay state and NOT the service state, to not get confused */
    private boolean isSwitchOverlayActive = false;
    /* Set overlay state to given state and with given animation speed */
    private void switchSetOverlay(boolean toState, boolean animate) {
        /* If not active move overlay outside view, to let it disappear */
        int newX = toState ? 0 : -switchLayoutOverlay.getMeasuredWidth();
        int duration = animate ? animationSpeed : 0;

        /* Save overlay state */
        isSwitchOverlayActive = toState;

        if (animate) {
            switchLayoutOverlay.animate().x(newX).setDuration(duration).setInterpolator(animationInterpolator);
            switchTextOverlay.animate().x(-newX).setDuration(duration).setInterpolator(animationInterpolator);
        } else {
            switchLayoutOverlay.setX(newX);
            switchTextOverlay.setX(-newX);
        }
    }

    private void onServiceStatusChange() {
        /*  If the service state is different than the switch state and is not performing an ongoing task,
            the switch is in the wrong state and thus synchronizes to the service state, to not confuse the
            user and perform the expected action on checking */
        if (serviceControl.getActive() != activeSwitch.isChecked() && !serviceControl.getWaiting()) {
            activeSwitch.setChecked(serviceControl.getActive());
        }

        /* Set overlay to actual service state */
        switchSetOverlay(serviceControl.getActive());

        /*  Implemented a yet unused waiting state, to be able to visualize an ongoing action from the service,
            in case this is necessary in the future */
        if (serviceControl.getWaiting()) {
            switchLayoutF.clearAnimation();
            switchLayoutF.animate().alpha(1f).setDuration(fastAnimationSpeed).setInterpolator(animationInterpolator).setStartDelay(0);
        } else {
            switchLayoutF.clearAnimation();
            switchLayoutF.animate().alpha(0f).setDuration(fastAnimationSpeed).setInterpolator(animationInterpolator).setStartDelay(animationSpeed);
        }
    }

    /* ------------------- */
    /* PROTECTED FUNCTIONS */

    @Override
    protected void onStart() {
        super.onStart();

        final Activity activity = this;

        /*  Show boot screen. We delay the actual start, so the boot screen is visible, before other tasks
            like the remote server check block the main thread. With this the start feels a lot smoother */
        new BootScreen(this, animationSpeed, 400, new Runnable() {
            @Override
            public void run() {
                /* Initialize preferences view */
                Preferences preferences = new Preferences(activity, TAG);
                ArrayList<Preferences.Item> preferenceItems = preferences.getItems();
                PreferencesView preferencesView = new PreferencesView(activity, preferenceItems, TAG, animationInterpolator, fastAnimationSpeed);
                gridview.setAdapter(preferencesView);

                /* Create new control instance, which binds to the service gets its state */
                serviceControl = new ServiceControl(activity);
                preferencesView.setServiceControl(serviceControl);

                /* Check for location permissions, and request them from the user if necessary */
                if (!(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAG, "No permissions. Requesting.");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }

                /* If this is minimum marshmallow use the nice looking feature to change the color of the status bar */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Window window = activity.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark, null));
                }

                /* Create Views for Status and Server Settings */
                new StatusClass(activity, TAG, serviceControl, Math.round(animationSpeed/2));

                /* Server settings get its own object we need to communicate with it */
                serverSettingsClass = new ServerSettingsClass(activity, TAG, serviceControl, Math.round(animationSpeed/2));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        /* Unbind from service */
        serviceControl.unbind();
    }

    /* Create app menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* Handle clicks on app menu items */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            new BootScreen(this, animationSpeed);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Set debug string to app name */
        TAG = getString(R.string.app_name);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Show app icon in toolbar */
        /*ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }*/

        /* Initialize all global Objects we need */
        switchLayoutOverlay = (FrameLayout) findViewById(R.id.layoutGrid_overlay);
        switchTextOverlay = (TextView) findViewById(R.id.active_switcher_label_overlay);
        switchLayoutF = (FrameLayout) findViewById(R.id.active_switcher_text_overlay);
        activeSwitch = (Switch) findViewById(R.id.active_switch);
        gridview = (GridView) findViewById(R.id.gridview);

        /* Called when the layout of the view overlay changes */
        switchLayoutOverlay.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                /*  If the width changes, reset the overlay, with the current state.
                    This is important, to give the overlay the correct size on startup,
                    which is used to move it outside the visible view */
                if ((right - left) != (oldRight - oldLeft)) {
                    switchOverlayRebuild();
                }
            }
        });

        /*  Listen to the switch, to activate/deactivate the Service
            Also visualises the activating/deactivating */
        activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    /* Starts service if necessary */
                    serviceControl.start();
                } else {
                    /* Stops service if necessary */
                    serviceControl.stop();
                }
            }
        });
    }

    /* ---------------- */
    /* PUBLIC FUNCTIONS */

    /*  Adjust the number of columns shown in the settings view, so they don't get too big.
        TODO The column numbers are hardcoded at the moment. */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        /* Check orientation and change the column number */
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridview.setNumColumns(5);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            gridview.setNumColumns(3);
        }
    }

    public void activeSwitchClick(View v) {
        /* Pass click of whole switch layout to switch */
        activeSwitch.performClick();
    }
}
