package cc.intx.owntrack;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatusClass {
    public String TAG;

    private Activity activity;
    private MainActivity.ServiceControl serviceControl;

    private GridLayout statusGrid;
    private TextView lastLocationTopPreview;
    private TextView lastLocationBotPreview;
    private TextView lastLocationFootPreview;
    private TextView lastLocationToSendTextView;
    private FrameLayout mapOverlay;
    private LinearLayout lastLocationDetails;

    private int extendingSpeed;

    public StatusClass(Activity activity, String TAG, MainActivity.ServiceControl serviceControl, int extendingSpeed) {
        this.TAG = TAG;
        this.activity = activity;
        this.serviceControl = serviceControl;
        this.extendingSpeed = extendingSpeed;

        statusGrid = (GridLayout) activity.findViewById(R.id.statusGrid);
        setOnClick();

        lastLocationTopPreview = (TextView) activity.findViewById(R.id.lastLocationTopPreview);
        lastLocationBotPreview = (TextView) activity.findViewById(R.id.lastLocationBotPreview);
        lastLocationFootPreview = (TextView) activity.findViewById(R.id.lastLocationFootPreview);
        lastLocationToSendTextView = (TextView) activity.findViewById(R.id.lastLocationToSendTextView);
        mapOverlay = (FrameLayout) activity.findViewById(R.id.mapOverlay);
        lastLocationDetails = (LinearLayout) activity.findViewById(R.id.lastLocationDetails);

        /* Set listener to receive location changes, so we can display them */
        serviceControl.setLastLocationListener(new Runnable() {
            @Override
            public void run() {
                loadLastLocation();
            }
        });
        loadLastLocation();
    }

    LocationReceiver.LocationData lastLocation;
    /* Get last location and display it */
    private void loadLastLocation() {
        lastLocation = serviceControl.getLastLocation();
        if (lastLocation != null) {
            try {
                String topText = "LATITUDE " + String.valueOf(lastLocation.getJSON().getDouble("lat"));
                String botText = "LONGITUDE " + String.valueOf(lastLocation.getJSON().getDouble("lon"));

                lastLocationTopPreview.setText(topText);
                lastLocationBotPreview.setText(botText);


                String toSendString = serviceControl.getToSendLocationsNumber() + " in send list";
                lastLocationToSendTextView.setText(toSendString);


                long dateDiff = (new java.util.Date()).getTime() - (new java.util.Date(lastLocation.getJSON().getLong("time"))).getTime();

                String footText;
                if (dateDiff <= 60 * 1000) {
                    footText = (dateDiff / 1000) + " seconds ago";
                } else if (dateDiff <= 60 * 60 * 1000) {
                    footText = (dateDiff / (60 * 1000)) + " minutes ago";
                } else if (dateDiff <= 99 * 60 * 60 * 1000) {
                    footText = (dateDiff / (60 * 60 * 1000)) + " hours ago";
                } else {
                    footText = "long ago";
                }

                lastLocationFootPreview.setText(footText);


                TextView lastLocationAccuracyTextView = (TextView) activity.findViewById(R.id.lastLocationAccuracyTextView);
                String accuracyString = "+/- " + String.valueOf(Math.round(lastLocation.getJSON().getDouble("accuracy") * 100) / 100) + " m";
                lastLocationAccuracyTextView.setText(accuracyString);


                TextView lastLocationSpeedTextView = (TextView) activity.findViewById(R.id.lastLocationSpeedTextView);
                Float speed = Math.round(lastLocation.getJSON().getDouble("speed") * 100) / 100f;

                /*  If speed is 0, the provider probably hasn't got any speed information
                    TODO Set speed to -1 if there is none and check for "speed >= 0" */
                if (speed > 0) {
                    String speedString = String.valueOf(speed) + " m/s";
                    lastLocationSpeedTextView.setText(speedString);
                } else {
                    lastLocationSpeedTextView.setText("Unknown");
                }


                TextView lastLocationProviderTextView = (TextView) activity.findViewById(R.id.lastLocationProviderTextView);
                String providerString = lastLocation.getJSON().getString("provider");
                providerString = providerString.substring(0,1).toUpperCase() + providerString.substring(1).toLowerCase();
                lastLocationProviderTextView.setText(providerString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int targetLastLocationDetailsHeight = 0;
    private boolean extended = false;
    public void onHeadClick() {
        if (targetLastLocationDetailsHeight == 0) {
            lastLocationDetails.measure(View.MeasureSpec.makeMeasureSpec(statusGrid.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.AT_MOST));
            targetLastLocationDetailsHeight = lastLocationDetails.getMeasuredHeight();
        }

        if (!extended) {
            Animation animation = new ShowAnimation(lastLocationDetails, targetLastLocationDetailsHeight, false);
            animation.setDuration(Math.round(extendingSpeed * 0.75));

            lastLocationDetails.clearAnimation();
            lastLocationDetails.startAnimation(animation);

            lastLocationToSendTextView.animate().alpha(0.6f).setDuration(extendingSpeed);
            mapOverlay.animate().alpha(1f).setDuration(extendingSpeed);

            extended = true;
        } else {
            Animation animation = new ShowAnimation(lastLocationDetails, targetLastLocationDetailsHeight, true);
            animation.setDuration(Math.round(extendingSpeed * 0.75));

            lastLocationDetails.clearAnimation();
            lastLocationDetails.startAnimation(animation);

            lastLocationToSendTextView.animate().alpha(0f).setDuration(extendingSpeed);
            mapOverlay.animate().alpha(0f).setDuration(extendingSpeed);

            extended = false;
        }
    }

    private void setOnClick() {
        activity.findViewById(R.id.lastLocationsHead1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHeadClick();
            }
        });

        activity.findViewById(R.id.lastLocationsHead2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHeadClick();
            }
        });
    }
}
