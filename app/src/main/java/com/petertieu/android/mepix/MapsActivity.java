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




public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{


    private static final String TAG = "MapsActivity";
    private static final String EXTRA_PIX_LATITUDE = "latitude";
    private static final String EXTRA_PIX_LONGITUDE = "longitude";
    private static final String EXTRA_PIX_ADDRESS = "address";

    public static final String EXTRA_PIX_NEW_LATITUDE = "newLatitude";
    public static final String EXTRA_PIX_NEW_LONGITUDE = "newLongitude";
    private static final int REQUEST_CODE_MAP = 5;

    //Declare GoogleMap object
    private GoogleMap mMap;

    //Declare double latitude of Pix location
    private double mLatitude;

    //Declare double longitude of Pix location
    private double mLongitude;

    //Declare String address of Pix location
    private String mAddress;

    //Declare marker for Pix location
    private MarkerOptions mPixLocationMarker;





    //Declare method to call in order to start this activity
    public static Intent newIntent(Context context, double latitude, double longitude, String address){

        //Log to Logcat
        Log.i(TAG, "newIntent(..) called");

        //Create new intent to start PixViewPagerActivity
        Intent intent = new Intent(context, MapsActivity.class);

        //Add lattitude as intent extra
        intent.putExtra(EXTRA_PIX_LATITUDE, latitude);

        //Add longitude as intent extra
        intent.putExtra(EXTRA_PIX_LONGITUDE, longitude);

        //Add address as intent extra
        intent.putExtra(EXTRA_PIX_ADDRESS, address);

        //Return the Intent
        return intent;
    }





    //Ovrride activity lifecycle callback method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set layout view of activity
        setContentView(R.layout.activity_maps);

        //Unpack intent extra then assign to latitude instance variable
        mLatitude = getIntent().getDoubleExtra(EXTRA_PIX_LATITUDE, 0);

        //Unpack intent extra then assign to longitude instance variable
        mLongitude = getIntent().getDoubleExtra(EXTRA_PIX_LONGITUDE, 0);

        //Unpack intent extra then assing to address instance variable
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

        //Set tite of Pix marker
        mPixLocationMarker.title(getString(R.string.pix_location) + " " + mAddress);

        //Allow Pix marker to be dragged
        mPixLocationMarker.draggable(true);

        //Add marker to map
        mMap.addMarker(mPixLocationMarker);



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










        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {


            @Override
            public void onMarkerDragStart(Marker marker) {
                Toast.makeText(MapsActivity.this, "Marker selected", Toast.LENGTH_SHORT).show();

            }


            @Override
            public void onMarkerDrag(Marker marker) {

            }


            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng location = marker.getPosition();


                Toast.makeText(getBaseContext(), "Lat " + location.latitude + " " + "Long " + location.longitude, Toast.LENGTH_LONG).show();

                Log.i(TAG, "Lat " + location.latitude + " " + "Long " + location.longitude);


                mPixLocationMarker.title("Lat " + location.latitude + " " + "Long " + location.longitude);

                sendResult(Activity.RESULT_OK, mLatitude, mLongitude);

            }

        });

    }


    private void sendResult(int resultCode, double newLatitude, double newLongitude) {

        Intent intent = new Intent();

        intent.putExtra(EXTRA_PIX_NEW_LATITUDE, newLatitude);
        intent.putExtra(EXTRA_PIX_NEW_LONGITUDE, newLongitude);

        onActivityResult(REQUEST_CODE_MAP, resultCode, intent);




    }








}
