package cc.intx.owntrack;

import android.Manifest;
import android.animation.TimeInterpolator;
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
    //Debug tag
    public String TAG;

    //Static variables
    final static private int animationSpeed = 600;
    final static private int fastAnimationSpeed = 200;
    final static private TimeInterpolator animationInterpolator = new FastOutSlowInInterpolator();

    //Global objects
    private FrameLayout switchLayoutOverlay;
    private FrameLayout switchLayoutF;
    private TextView switchTextOverlay;
    private GridView gridview;
    private Switch activeSwitch;
    private PreferencesView preferencesView;

    //Extend control service class, so we can use this class variables more easily
    public class ServiceControl extends ServiceControlClass {
        public ServiceControl(Context context) {
            //Copy parent constructor
            super(context, TAG);
        }

        public void onChangeStatus() {
            //Implement onstatuschange action
            onServiceStatusChange();
        }
    }
    private ServiceControl serviceControl;

    /*
    PRIVATE FUNCTIONS
     */
    private void switchOverlayRebuild() {switchSetOverlay(isSwitchOverlayActive, false);}//Rebuild overlay (e.g. on layout changes) with current state
    private void switchSetOverlay(boolean toState) {switchSetOverlay(toState, true);}//Switch state and animate said change
    /*
    Set overlay state to given state and with given animation speed
     */
    private boolean isSwitchOverlayActive = false;//This signalizes only the overlay state and NOT the service state, to not get confused
    private void switchSetOverlay(boolean toState, boolean animate) {
        int newX = toState ? 0 : -switchLayoutOverlay.getMeasuredWidth();//If not active move overlay outside view, to let it disappear
        int duration = animate ? animationSpeed : 0;

        isSwitchOverlayActive = toState;//Save overlay state

        if (animate) {
            switchLayoutOverlay.animate().x(newX).setDuration(duration).setInterpolator(animationInterpolator);
            switchTextOverlay.animate().x(-newX).setDuration(duration).setInterpolator(animationInterpolator);
        } else {
            switchLayoutOverlay.setX(newX);
            switchTextOverlay.setX(-newX);
        }
    }

    private void onServiceStatusChange() {
        /*
        If the service state is different than the switch state and is not performing an ongoing task,
        the switch is in the wrong state and thus synchronizes to the service state, to not confuse the
        user and perform the expected action on checking
         */
        if (serviceControl.getActive() != activeSwitch.isChecked() && !serviceControl.getWaiting()) {
            activeSwitch.setChecked(serviceControl.getActive());
        }

        //Set overlay to actual service state
        switchSetOverlay(serviceControl.getActive());

        /*
        Implemented a yet unused waiting state, to be able to visualize an ongoing action from the service,
        in case this is necessary in the future
         */
        if (serviceControl.getWaiting()) {
            switchLayoutF.clearAnimation();
            switchLayoutF.animate().alpha(1f).setDuration(fastAnimationSpeed).setInterpolator(animationInterpolator).setStartDelay(0);
        } else {
            switchLayoutF.clearAnimation();
            switchLayoutF.animate().alpha(0f).setDuration(fastAnimationSpeed).setInterpolator(animationInterpolator).setStartDelay(animationSpeed);
        }
    }

    /*
    PROTECTED FUNCTIONS
     */
    @Override
    protected void onStart() {
        super.onStart();

        //Create new control instance, which binds to the service gets its state
        serviceControl = new ServiceControl(this);
        preferencesView.setServiceControl(serviceControl);

        if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Log.d(TAG, "No permissions. Requesting.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDark, null));
        }

        //Create Views for Status and Server Settings
        new StatusClass(this, TAG, serviceControl, Math.round(animationSpeed/2));
        new ServerSettingsClass(this, TAG, serviceControl, Math.round(animationSpeed/2));
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Unbind from service
        serviceControl.unbind();
    }

    /*
    Adjust the number of columns shown in the settings view, so they don't get too big.
    TODO The column numbers are hardcoded at the moment.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //Check orientation and change the column number
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridview.setNumColumns(5);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            gridview.setNumColumns(3);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = getString(R.string.app_name); //Set debug string to app name

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize all global Objects we need
        switchLayoutOverlay = (FrameLayout) findViewById(R.id.layoutGrid_overlay);
        switchTextOverlay = (TextView) findViewById(R.id.active_switcher_label_overlay);
        switchLayoutF = (FrameLayout) findViewById(R.id.active_switcher_text_overlay);
        activeSwitch = (Switch) findViewById(R.id.active_switch);
        Preferences preferences = new Preferences(this, TAG);

        //Initialize settings
        gridview = (GridView) findViewById(R.id.gridview);
        ArrayList<Preferences.Item> preferenceItems = preferences.getItems();
        preferencesView = new PreferencesView(this, preferenceItems, TAG, animationInterpolator, fastAnimationSpeed);
        gridview.setAdapter(preferencesView);

        //Called when the layout of the view overlay changes
        switchLayoutOverlay.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                /*
                If the width changes, reset the overlay, with the current state.
                This is important, to give the overlay the correct size on startup,
                which is used to move it outside the visible view
                 */
                if ((right - left) != (oldRight - oldLeft)) {
                    switchOverlayRebuild();
                }
            }
        });

        /*
        Listen to the switch, to activate/deactivate the Service
        Also visualises the activating/deactivating
         */
        activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Starts service if necessary
                    serviceControl.start();
                } else {
                    //Stops service if necessary
                    serviceControl.stop();
                }
            }
        });
    }

    /*
    PUBLIC FUNCTIONS
     */
    public void activeSwitchClick(View v) {
        //Pass click of whole switch layout to switch
        activeSwitch.performClick();
    }
}
