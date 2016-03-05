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

    private final static String LOCATION_LIST_FILENAME = "locationlist";

    private Context context;
    private LocationManager locationManager;
    private TrackingLocationListener locationListener;

    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;

    private boolean gpsPermissions = false;
    private boolean networkPermissions = false;

    private Location networkLocation;
    private Location gpsLocation;

    private JSONArray locationList;

    private int abortLocationUpdateTime = 10 * 1000;//10 seconds

    public class LocationData {
        JSONObject locationData = new JSONObject();

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

    public void getPermissions() {
        gpsPermissions = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        networkPermissions = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private Runnable newLocationListener;
    public void getLocation(Runnable newLocationListener) {
        this.newLocationListener = newLocationListener;
        getPermissions();

        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && gpsPermissions;
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && networkPermissions;

        Handler handler = new Handler();
        Runnable stopLocationUpdate = new Runnable() {
            @Override
            public void run() {
                if (gpsPermissions || networkPermissions) {
                    try {
                        locationManager.removeUpdates(locationListener);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }

                gotLocation();
            }
        };

        gpsLocation = null;
        networkLocation = null;
        try {
            if (isGpsEnabled) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }

            if (isNetworkEnabled) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        handler.postDelayed(stopLocationUpdate, abortLocationUpdateTime);
    }

    private JSONArray getLocationList() {
        JSONArray locationList = new JSONArray();

        FileInputStream fileInputStream;
        try {
            fileInputStream = context.openFileInput(LOCATION_LIST_FILENAME);

        } catch (FileNotFoundException e) {
            fileInputStream = null;
        }

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

        return locationList;
    }

    private void saveLocation(Location location) {
        locationList = getLocationList();

        LocationData locationData = new LocationData(location);
        locationList.put(locationData.getJSON());

        this.lastLocation = locationData;

        try {
            FileOutputStream fileOutputStream = context.openFileOutput(LOCATION_LIST_FILENAME, Context.MODE_PRIVATE);
            fileOutputStream.write(locationList.toString().getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gotLocation() {
        if (gpsLocation != null || networkLocation != null) {
            Location newLocation;

            if (gpsLocation != null && networkLocation != null) {
                newLocation = gpsLocation.getAccuracy() < networkLocation.getAccuracy() ? gpsLocation : networkLocation;
            } else {
                newLocation = gpsLocation == null ? networkLocation : gpsLocation;
            }

            saveLocation(newLocation);
        }

        if (newLocationListener != null) {
            newLocationListener.run();
        }
    }

    public LocationReceiver(String TAG, Context context) {
        this.TAG = TAG;

        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new TrackingLocationListener();

        getPermissions();
    }

    private LocationData lastLocation;
    public LocationData getLastLocation() {
        return lastLocation;
    }
}
