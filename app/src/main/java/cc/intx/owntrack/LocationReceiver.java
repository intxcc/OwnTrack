package cc.intx.owntrack;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class LocationReceiver {
    private String TAG;

    //File to save the list of locations to upload
    private final static String LOCATION_LIST_FILENAME = "locationlist";

    //Application communication objects
    private Context context;
    private LocationManager locationManager;
    private TrackingLocationListener locationListener;
    private SendLocation sendLocation;

    //Runnable to execute when the current locations change
    //Attention, this is not REALLY global, as it is set by getLocation, and therefor should only be used in reaction to getLocation
    private Runnable newLocationListener;

    //Save the upload interval
    private int uploadInterval = 0;

    //Save the last location for the main activity to grab and display for information purposes
    private LocationData lastLocation;

    private boolean gpsPermissions = false;
    private boolean networkPermissions = false;

    //Objects to save received locations
    private Location networkLocation;
    private Location gpsLocation;

    //List to save all locations waiting for upload
    private JSONArray locationList;

    //Creates a JSON object for every location for better handling and upload
    public class LocationData {
        JSONObject locationData = new JSONObject();

        public LocationData(JSONObject jsonObject) {
            locationData = jsonObject;
        }

        public LocationData(Location location) {
            try {
                this.locationData.put("lat", location.getLatitude());
                this.locationData.put("lon", location.getLongitude());

                this.locationData.put("speed", location.getSpeed());
                this.locationData.put("accuracy", location.getAccuracy());

                this.locationData.put("time", location.getTime());
                this.locationData.put("provider", location.getProvider());
            } catch (Exception e) {
                e.printStackTrace();
                locationData = null;
            }
        }

        public JSONObject getJSON() {
            return locationData;
        }
    }

    //Location listener to handle location updates from LocationManager
    private class TrackingLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                gpsLocation = loc;
            } else if (loc.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                networkLocation = loc;
            } else {
                Log.d(TAG, "Unexpected location provider: " + loc.getProvider());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    //Check self permissions
    public void getPermissions() {
        gpsPermissions = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        networkPermissions = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    //Main function of this class, to update the current location and prepare it for upload
    public void getLocation(Runnable newLocationListener, int abortAfterMs) {

        //This is a callback method, because the location is fetched asynchron
        this.newLocationListener = newLocationListener;
        getPermissions();

        //Check if the providers are enabled, but only use them if we got the permissions to do so
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && gpsPermissions;
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && networkPermissions;

        //Create handler, to abort the location update after a custom time, to reduce battery usage
        Handler handler = new Handler();
        Runnable stopLocationUpdate = new Runnable() {
            @Override
            public void run() {
                //If we got any permissions we added a location listener. Remove updates should remove all listeners added
                if (gpsPermissions || networkPermissions) {
                    try {
                        locationManager.removeUpdates(locationListener);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }

                //Callback to handle new locations just received
                gotLocation();
            }
        };

        //Initialize network and gps location. Later we choose the one which got an update, but always prefer gps, as it has more information like speed
        gpsLocation = null;
        networkLocation = null;
        try {
            //Register the listeners to receive a location update

            if (isGpsEnabled) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }

            if (isNetworkEnabled) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        //Stop the time to abort the location update
        handler.postDelayed(stopLocationUpdate, abortAfterMs);
    }

    //Load the location list from the file system
    private JSONArray getLocationList() {
        JSONArray locationList = new JSONArray();

        FileInputStream fileInputStream;
        try {
            fileInputStream = context.openFileInput(LOCATION_LIST_FILENAME);
        } catch (FileNotFoundException e) {
            fileInputStream = null;
        }

        //TODO else - can't open file error handling
        //Load file content and parse the JSONArray
        if (fileInputStream != null) {
            StringBuilder builder = new StringBuilder();
            int ch;

            try {
                while ((ch = fileInputStream.read()) != -1) {
                    builder.append((char) ch);
                }

                //close file
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                locationList = new JSONArray(builder.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Return the list - on error it is just empty
        return locationList;
    }

    //Is called whenever the service indicates setting changes
    public void changeUploadInterval(int newInterval) {
        uploadInterval = newInterval;
    }

    //Public method to start an attempt to upload the locations
    /* TODO error handling if the service is running, but not the activity, to show the error the
       next time the activity is started */
    public void upload() {
        //Get the fresh list from the file system
        JSONArray locationList = getLocationList();

        //If it is not null and has more entrys then the settings specify as the number of locations at which to upload -> upload
        if (locationList != null && locationList.length() >= uploadInterval) {
            //Attempt upload
            sendLocation.upload(locationList);

            //Get upload errors
            int result = sendLocation.getLastError();

            //If there are none, we uploaded successfully and can clear the list now
            if (result == 0) {
                clearAllLocations();
            } else {
                //Print error to debug log
                Log.d(TAG, "Error: " + result);
            }
        }
    }

    //Clear the location list. Just creates an empty location list and saves it
    private void clearAllLocations() {
        locationList = new JSONArray();
        saveLocationList();
    }

    //Append the given location to the location list
    private void saveLocation(Location location) {
        //Get fresh list from file system
        locationList = getLocationList();

        //Append the location in form of JSONObject, for better handling
        LocationData locationData = new LocationData(location);
        locationList.put(locationData.getJSON());

        //Change the last location, to show in the activity
        this.lastLocation = locationData;

        saveLocationList();
    }

    //Save locationList as JSON string to filesystem
    private void saveLocationList() {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(LOCATION_LIST_FILENAME, Context.MODE_PRIVATE);
            fileOutputStream.write(locationList.toString().getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Callback after location update was "aborted". The updated is "aborted", even if it was successful
    private void gotLocation() {
        //If we got a new location, choose the one that is not empty, and if both providers got a location choose the one with the better (smaller) accuracy
        if (gpsLocation != null || networkLocation != null) {
            Location newLocation;

            if (gpsLocation != null && networkLocation != null) {
                newLocation = gpsLocation.getAccuracy() < networkLocation.getAccuracy() ? gpsLocation : networkLocation;
            } else {
                newLocation = gpsLocation == null ? networkLocation : gpsLocation;
            }

            //Save the location
            saveLocation(newLocation);
        }

        //Callback for activity to react to location updates
        if (newLocationListener != null) {
            newLocationListener.run();
        }
    }

    //Constructor gets a sendLocation object, instead of creating its own, so the main service class can communicate with the sendlocation more easily
    public LocationReceiver(String TAG, Context context, SendLocation sendLocation) {
        this.TAG = TAG;

        this.context = context;
        this.sendLocation = sendLocation;

        /* Get the Location service and create ONE listener instance to later register for updates.
           Ine instance, so we can remove it more easily from both providers and because 2 would be not any better */
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new TrackingLocationListener();

        //Initialize permissions. This is also done whenever we do something requiring permissions
        getPermissions();
    }

    //To allow the activity to get the number of locations waiting for upload
    public int getListSize() {
        return locationList.length();
    }

    //Load the last location fresh from the file system
    public LocationData getLastLocation() {
        locationList = getLocationList();

        //If we got at least last location load the last in the location list array
        if (locationList.length() >= 1) {
            try {
                //Convert the lastLocation to our uniform LocationData format to unify the location handling
                lastLocation = new LocationData(new JSONObject(locationList.get(locationList.length() - 1).toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lastLocation;
    }
}
