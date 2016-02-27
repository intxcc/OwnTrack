package cc.intx.owntrack;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //Debug tag
    private String TAG;

    //Static variables
    final static private int animationSpeed = 600;
    final static private int fastAnimationSpeed = 200;
    final static private TimeInterpolator animationInterpolator = new FastOutSlowInInterpolator();

    //Global objects
    private FrameLayout switchLayoutOverlay;
    private FrameLayout switchLayoutF;
    private TextView switchTextOverlay;
    private Switch activeSwitch;

    //Control service class
    private class ServiceControlClass {
        private boolean isServiceActive = false;
        private boolean isServiceWaiting = false;

        private void changeActiveStatus(boolean isActive) {
            isServiceActive = isActive;
            isServiceWaiting = false;

            onChangeStatus();
        }

        private void changeStatusToWaiting(){
            isServiceWaiting = true;

            onChangeStatus();
        }

        private void onChangeStatus() {
            onServiceStatusChange();
        }

        private void started() {
            Log.d(TAG, "Started service.");

            changeActiveStatus(true);
        }

        private void stopped() {
            Log.d(TAG, "Stopped service.");

            changeActiveStatus(false);
        }

        private void start() {
            changeStatusToWaiting();
            Log.d(TAG, "Starting service...");

            Context context = getBaseContext();
            Intent intent = new Intent(context, TrackingService.class);
            startService(intent);

            //started();
        }

        private void stop() {
            Log.d(TAG, "Stopping service...");

            Context context = getBaseContext();
            Intent intent = new Intent(context, TrackingService.class);
            stopService(intent);

            //stopped();
        }

        public boolean getActive() {
            return isServiceActive;
        }

        public boolean getWaiting() {
            return isServiceWaiting;
        }
    }
    final private ServiceControlClass serviceControl = new ServiceControlClass();

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

        switchLayoutOverlay.animate().x(newX).setDuration(duration).setInterpolator(animationInterpolator);
        switchTextOverlay.animate().x(-newX).setDuration(duration).setInterpolator(animationInterpolator);
    }

    private void onServiceStatusChange() {
        switchSetOverlay(serviceControl.getActive());

        if (serviceControl.getWaiting()) {
            activeSwitch.setEnabled(false);
            activeSwitch.setClickable(false);
            activeSwitch.setFocusable(false);

            switchLayoutF.clearAnimation();
            switchLayoutF.animate().alpha(1f).setDuration(fastAnimationSpeed).setInterpolator(animationInterpolator).setStartDelay(0);
        } else {
            activeSwitch.setEnabled(true);
            activeSwitch.setClickable(true);
            activeSwitch.setFocusable(true);

            switchLayoutF.clearAnimation();
            switchLayoutF.animate().alpha(0f).setDuration(fastAnimationSpeed).setInterpolator(animationInterpolator).setStartDelay(animationSpeed);
        }
    }

    /*
    PROTECTED FUNCTIONS
     */
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
                    serviceControl.start();
                } else {
                    serviceControl.stop();
                }
            }
        });
    }

    /*
    PUBLIC FUNCTIONS
     */
    public void activeSwitchClick(View v) {
        activeSwitch.performClick();//Pass click of whole switch layout to switch
    }
}
