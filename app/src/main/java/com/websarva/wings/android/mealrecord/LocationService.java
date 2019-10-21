package com.websarva.wings.android.mealrecord;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class LocationService extends Service implements LocationListener {
    //private ContextAct mConAct;


    // flag for GPS Status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    android.location.Location location;
    double latitude;
    double longitude;

    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE=1232;
    // Declaring a Location Manager
    protected LocationManager locationManager;

    ContextAct mconAct;
    public LocationService() {
        getLocation();
    }

    public android.location.Location getLocation() {


        try {
            locationManager = (LocationManager) ContextAct.getmContext()
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            List<String> reqPermissions = new ArrayList<>();

            if (!isGPSEnabled && !isNetworkEnabled) {
                // location service disabled
            } else {
                this.canGetLocation = true;

                // if GPS Enabled get lat/long using GPS Services
                reqPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

                if (isGPSEnabled) {
                    if (ActivityCompat.checkSelfPermission(ContextAct.getmContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ContextAct.getmContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        ActivityCompat.requestPermissions(ContextAct.getmActivity(),
                                reqPermissions.toArray(new String[reqPermissions.size()]),
                                LOCATION_PERMISSION_REQUEST_CODE);
                    }

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this);

                    Log.d("GPS Enabled", "GPS Enabled");

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        //updateGPSCoordinates();
                    }
                }

                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("Network", "Network");

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            //updateGPSCoordinates();
                        }
                    }

                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            Log.e("Error : Location",
                    "Impossible to connect to LocationManager", e);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 50, this);

        return location;
    }



    public void updateGPSCoordinates(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void onLocationChanged(Location location) {
        //Toast.makeText(getApplicationContext(),  "not eng" + location.toString(), Toast.LENGTH_LONG).show();
        //updateGPSCoordinates(Location location);

        this.location = location;

        Log.d("changed--longitude ",String.valueOf(location.getLongitude()));
        Log.d("changed--latitude ",String.valueOf(location.getLatitude()));
        updateGPSCoordinates(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public IBinder onBind(Intent intent) {
        return null;
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // start to find location...
                getLocation();

            } else { // if permission is not granted
                Toast.makeText(this, "位置情報取得が拒否されました", Toast.LENGTH_SHORT).show();

                // decide what you want to do if you don't get permissions
            }
        }
    }
}
