package com.mahesh_prajapati.mopboxexamples.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;



/**
 * Created by m.prajapati on 06-04-2018.
 */

public abstract class BaseActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback,
        PermissionUtils.PermissionResultCallback{






    // LogCat tag
    private static final String TAG = BaseActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_REQUEST = 1000;
    private final static int REQUEST_CHECK_SETTINGS = 2000;

    private Location mLastLocation;


    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // list of permissions
    ArrayList<String> permissions=new ArrayList<>();
    PermissionUtils permissionUtils;
    boolean isPermissionGranted;
    private LocationRequest mLocationRequest;
    public boolean internetStatus;
    public static android.widget.TextView log_network;







    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         initBaseActivityValues(true,true);
    }








    public void initBaseActivityValues(boolean isNeedPermissions,boolean isNeedLocation){


        if(isNeedPermissions){
            checkForPermission(true);
        }

        if(isNeedLocation){
            final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                updatedGpsStatus(false);
                checkGpsOnOFF();
            }else{
                initGoogleApiClient();
                checkGpsOnOFF();
            }

        }


    }



    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;



    public void initGoogleApiClient(){

        if (checkPlayServices()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                getLocation();
            }else{
                // Building the GoogleApi client
                buildGoogleApiClient();
            }
        }
    }



    public void checkGpsOnOFF(){
        GpsChangeReceiver m_gpsChangeReceiver = new GpsChangeReceiver();
        getApplicationContext().registerReceiver(m_gpsChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    public class GpsChangeReceiver   extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent )
        {
            final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
            if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                updatedGpsStatus(true);
            }
            else
            {
                updatedGpsStatus(false);
            }
        }
    }

    public void updatedGpsStatus(boolean isGpsOn) {
        if(!isGpsOn){
           // initGoogleApiClient();
          //  checkGpsOnOFF();
        }
    }




    public boolean checkPlayServices() {
        com.google.android.gms.common.GoogleApiAvailability googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this,resultCode,
                        PLAY_SERVICES_REQUEST).show();} else {
               /* UiHelper.vibrateApplication(MapsActivity.this);
                UiHelper.showdialog(MapsActivity.this,getString(R.string.sorry),getString(R.string.play_store_error));*/
            }
            return false;
        }
        return true;
    }




    /**
     * Creating google api client object
     * */

    @SuppressLint("RestrictedApi")
    protected synchronized void buildGoogleApiClient() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        final boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        updatedGpsStatus(statusOfGPS);
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
        if(mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(50);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        // **************************
        // builder.setAlwaysShow(true); // this is the key ingredient
        // **************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location requests here
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            getFinalLoaction();
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(BaseActivity.this, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            android.util.Log.e(TAG,""+e);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        android.util.Log.e(TAG,"");
                        break;
                }
            }
        });


    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        getFinalLoaction();

                        // btGPS.setTextColor(Color.parseColor("#008000"));
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        // btGPS.setTextColor(Color.RED);
                        break;
                    default:
                        //  btGPS.setTextColor(Color.RED);
                        break;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void getFinalLoaction() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mGoogleApiClient.isConnected()) {
                checkForPermission(true);
                getLocation(); /* commented by rajeev */
            }

        } else {
            getLocation();
        }

    }

    public void checkForPermission(boolean isdialog) {
        permissionUtils=new PermissionUtils(BaseActivity.this);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
       // permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissionUtils.check_permission(permissions,"Please allow location permission",1);
    }


    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        android.util.Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(android.os.Bundle arg0) {

        // Once connected with google api, get the location
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    // Permission check functions


    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions,
                                           @androidx.annotation.NonNull int[] grantResults) {// redirects to utils

        if(permissions!=null){
            permissionUtils.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }


    }



    @SuppressLint("MissingPermission")
    private void getLocation() {
        if (isPermissionGranted) {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
            final boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            updatedGpsStatus(statusOfGPS);
            try
            {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

                    LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
                    criteria.setPowerRequirement(Criteria.POWER_HIGH);
                    criteria.setAltitudeRequired(false);
                    criteria.setSpeedRequired(false);
                    criteria.setCostAllowed(true);
                    criteria.setBearingRequired(false);
                    criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                    criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
                    mLocationManager.requestLocationUpdates(5000, 10, criteria,new LocationListener() {
                        @Override
                        public void onLocationChanged(final Location location) {
                            mLastLocation = location;
                            if (mLastLocation != null) {
                                   updatedLocation(location);
                            } else {

                            }
                        }

                        @Override
                        public void onStatusChanged(String s, int i, android.os.Bundle bundle) {

                        }

                        @Override
                        public void onProviderEnabled(String s) {

                        }

                        @Override
                        public void onProviderDisabled(String s) {

                        }
                    },null);
                }else{
                    FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            Location locationData = locationResult.getLastLocation();

                            mLastLocation = locationData ;
                            if (mLastLocation != null) {
                                updatedLocation(locationData );
                            } else {

                            }

                        }
                    },null);
                    /*LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onLocationChanged(Location location) {

                        }
                    });*/
                }
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }

        }else {
          //  permissionStatus(1);
        }

    }

    public void permissionStatus(int isPermissionGranted){

    }

    public void updatedLocation(Location location) {
    }


    @Override
    public void PermissionGranted(int request_code) {
         permissionStatus(0);
         isPermissionGranted=true;
         getLocation();
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {}

    @Override
    public void PermissionDenied(int request_code) {
        android.util.Log.i("PERMISSION_CHECK","DENIED");
        permissionStatus(1);
    }

    @Override
    public void NeverAskAgain(int request_code) {
        android.util.Log.i("PERMISSION_CHECK","NEVER ASK AGAIN");
        permissionStatus(3);
    }


}
