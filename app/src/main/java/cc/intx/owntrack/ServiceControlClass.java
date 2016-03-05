package cc.intx.owntrack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

abstract class ServiceControlClass {
    //Debug tag
    private String TAG;

    //State variables
    private boolean isServiceActive = false;
    private boolean isServiceWaiting = false;

    //The service interface, with which we can use the services public functions to communicate
    private TrackingService trackingService;
    private Context context;//The service context
    private Intent serviceIntent;//The service intent

    //This is implemented by the main activity, so we can visualize changed service states
    public abstract void onChangeStatus();
    /*
    This runnable is passed to the service on bind, so the service can pass status changes to the
    service control. The runnable will also always get called on onbind, to get the current state
     */
    private Runnable changeStatusListener = new Runnable() {
        @Override
        public void run() {
            //React to status changes
            if (trackingService != null) {
                changeActiveStatus(trackingService.getIsRunning());
            }
        }
    };

    private Runnable newLocationListener = new Runnable() {
        @Override
        public void run() {
            if (trackingService != null) {
                LocationReceiver.LocationData locationData = trackingService.getLastLocation();
                if (locationData != null) {
                    setLastLocation(locationData);
                }
            }
        }
    };

    abstract void setLastLocation(LocationReceiver.LocationData locationData);

    //This represents the binding state of the service connection. This does not indicate an active service
    private boolean isBound = false;
    /*
    The service connection binds to the service, receives its public interface and also will pass
    the status change listener to the service.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            /*
            Cast IBinder to the TrackingService binder implementation.
            This will throw an error if the service runs in a different process
             */
            TrackingService.ServiceBinder binder = (TrackingService.ServiceBinder) service;
            trackingService = binder.getService(); //Retrieve the services public interface

            //Pass status change listener
            trackingService.setIsRunningListener(changeStatusListener);
            trackingService.setNewLocationListener(newLocationListener);

            //Change connection state
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //This is only called if the service disconnects UNEXPECTED
            //TODO implement a better way to handle unexpected disconnects
            trackingService = null;//Remove tracking service, to not get unexpected behaviour
            isBound = false; //Change connection state
        }
    };

    public ServiceControlClass(Context context, String TAG) {
        this.context = context;//Get context, with which to handle the service
        serviceIntent = new Intent(context, TrackingService.class);
        this.TAG = TAG;//To reduce duplicates

        bind();//Bind on creating. Does create the service, but not start
    }

    /*
    Bind service, with no change to its priority and auto creation, so we can do all the processing
    there, as the service is the main component
     */
    private void bind() {
        context.bindService(serviceIntent, serviceConnection, Context.BIND_WAIVE_PRIORITY | Context.BIND_AUTO_CREATE);
    }

    /*
    Unbind Service. Is only called when the application closes
     */
    public void unbind() {
        context.unbindService(serviceConnection);
        trackingService = null;
        isBound = false;
    }

    /*
    Start the service and if we are not already bound or we lost connection
     */
    public void start() {
        if (!getActive()) {
            changeStatusToWaiting();
            Log.d(TAG, "Starting service...");

            context.startService(serviceIntent);

            if (!isBound) {
                bind();
            }
        }
    }

    /*
    Stops the service. IMPORTANT this will not actually stop the service as stated in the doc, but
    will only stop the function of the tracking service: cancel all scheduled wakeups to track location.
    This is, so we can still communicate with it, change prefs, show stats and so on. If the app unbinds
    now the service will actually stop, as in destroy and free resources.
     */
    public void stop() {
        if (getActive()) {
            Log.d(TAG, "Stopping service...");

            if (trackingService == null) {
                context.stopService(serviceIntent);
            } else {
                trackingService.stopService();
            }
        }
    }

    public void changedSettings() {
        if (trackingService != null) {
            trackingService.changedSettings();
        }
    }

    //Get active state
    public boolean getActive() {
        return isServiceActive;
    }

    //Get waiting state
    public boolean getWaiting() {
        return isServiceWaiting;
    }

    //Change active status and remove waiting state. Noop if no change
    private void changeActiveStatus(boolean isActive) {
        if (isServiceActive != isActive || isServiceWaiting) {
            isServiceActive = isActive;
            isServiceWaiting = false;

            onChangeStatus();
        }
    }

    //Change status to waiting
    private void changeStatusToWaiting(){
        if (!isServiceWaiting) {
            isServiceWaiting = true;

            onChangeStatus();
        }
    }
}