package com.petertieu.android.mepix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.UUID;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{


    private static final String TAG = "MapsActivity";
    private static final String EXTRA_PIX_ID = "pixId";
    private static final String EXTRA_PIX_LATITUDE = "latitude";
    private static final String EXTRA_PIX_LONGITUDE = "longitude";
    private static final String EXTRA_PIX_ADDRESS = "address";


    //Declare GoogleMap object
    private GoogleMap mMap;

    //Declare UUID of Pix
    private UUID mPixId;

    //Declare double latitude of Pix location
    private double mLatitude;

    //Declare double longitude of Pix location
    private double mLongitude;

    //Declare String address of Pix location
    private String mAddress;

    //Declare marker for Pix location
    private MarkerOptions mPixLocationMarker;

    private LatLng mMarkerLocation;





    //Declare method to call in order to start this activity
    public static Intent newIntent(Context context, UUID pixId, double latitude, double longitude, String address){

        //Log to Logcat
        Log.i(TAG, "newIntent(..) called");

        //Create new intent to start PixViewPagerActivity
        Intent intent = new Intent(context, MapsActivity.class);

        //Add UUID of Pix as intent extra
        intent.putExtra(EXTRA_PIX_ID, pixId);

        //Add latitude as intent extra
        intent.putExtra(EXTRA_PIX_LATITUDE, latitude);

        //Add longitude as intent extra
        intent.putExtra(EXTRA_PIX_LONGITUDE, longitude);

        //Add address as intent extra
        intent.putExtra(EXTRA_PIX_ADDRESS, address);

        //Return the Intent
        return intent;
    }





    //Override activity lifecycle callback method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout view of activity
        setContentView(R.layout.activity_maps);

        //Unpack intent extra then assign to UUID instance variable
        mPixId = (UUID) getIntent().getSerializableExtra(EXTRA_PIX_ID);

        //Unpack intent extra then assign to latitude instance variable
        mLatitude = getIntent().getDoubleExtra(EXTRA_PIX_LATITUDE, 0);

        //Unpack intent extra then assign to longitude instance variable
        mLongitude = getIntent().getDoubleExtra(EXTRA_PIX_LONGITUDE, 0);

        //Unpack intent extra then passing to address instance variable
        mAddress = getIntent().getStringExtra(EXTRA_PIX_ADDRESS);

        //Obtain map from SupportMapFragment, and pass associated resource ID to display the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //Call getMapAsync to set callback that's triggered once GoogleMap instance is ready to use
        mapFragment.getMapAsync(this);
    }





     //Manipulate map once available. User will be prompted to download Google Play Services if not installed.
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Assign mMap reference variable to GoogleMap object
        mMap = googleMap;

        //Add marker in Sydney and move the camera
        LatLng pixLocationLatLon = new LatLng(mLatitude, mLongitude);

        //Create marker for Pix location
        mPixLocationMarker = new MarkerOptions();

        //Set position of Pix marker
        mPixLocationMarker.position(pixLocationLatLon);

        //Set title of Pix marker
        mPixLocationMarker.title(getString(R.string.pix_location) + " " + mAddress);

        //Add marker to map
        mMap.addMarker(mPixLocationMarker).showInfoWindow();



        //Define magnitude of extra bounds to control camera zoom level
        double offsetFromLocation = 0.00003f;
        //Extra bound #1
        LatLng northEastLatLon = new LatLng(mLatitude + offsetFromLocation, mLongitude + offsetFromLocation);
        //Extra bound #2
        LatLng southWestLatLon = new LatLng(mLatitude - offsetFromLocation, mLongitude - offsetFromLocation);


        //Define a rectangular lat/lon boundary perimeter, where the to accommodate the bounds
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                                    .include(pixLocationLatLon)
                                                    .include(northEastLatLon)
                                                    .include(southWestLatLon)
                                                    .build();

        //Define the The padding of the 'bounds' perimeter.
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);

        //Set camera to lat/lon bounds
        CameraUpdate updateCamera = CameraUpdateFactory.newLatLngBounds(latLngBounds, margin);

        //Animate/move camera to display lat/lon bounds
        mMap.animateCamera(updateCamera);
    }




    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause() called");
    }



    @Override
    public void onStop(){
        super.onStop();

    }




}
