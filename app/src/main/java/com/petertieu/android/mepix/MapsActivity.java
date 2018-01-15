package com.petertieu.android.mepix;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final String TAG = "MapsActivity";
    private static final String EXTRA_PIX_LOCATION = "location";
    private static final String EXTRA_PIX_ADDRESS = "address";

    private GoogleMap mMap;

    private Location mLocation;

    private String mAddress;






    //Method to call for
    public static Intent newIntent(Context context, Location location, String address){

        //Log to Logcat
        Log.i(TAG, "newIntent(..) called");

        //Create new intent to start PixViewPagerActivity
        Intent intent = new Intent(context, MapsActivity.class);

        //Add extra to intent
        intent.putExtra(EXTRA_PIX_LOCATION, location);

        intent.putExtra(EXTRA_PIX_ADDRESS, address);

        //Return the Intent
        return intent;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        onRetainCustomNonConfigurationInstance();
        onRetainNonConfigurationInstance();



        mLocation = getIntent().getParcelableExtra(EXTRA_PIX_LOCATION);

        mAddress = getIntent().getStringExtra(EXTRA_PIX_ADDRESS);

        //Obtain map from SupportMapFragment, and pass associated resource ID to display the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //Call getMapAsync to set callback that's triggered once GoogleMap instance is ready to use
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Assign mMap reference variable to GoogleMap object
        mMap = googleMap;

        //Add marker in Sydney and move the camera
        LatLng locationLatLon = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

        //Add marker
        mMap.addMarker(new MarkerOptions().position(locationLatLon).title(getString(R.string.pix_location) + mAddress));

//        //Move camera to marker
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationLatLon));


        //Define a rectangular lat-lon boundary perimeter, where the following TWO POINTS lie at the perimeter of this rectangle:
        //1: The location point of the PHOTO (itemPoint)
        //2: The location point of the CURRENT LOCATION (myPoint)
        //NOTE: LatLngBounds extends Object
        //NOTE: The LatLngBounds.include(..) methods actually include the location points in which the LatLngBounds should encompass.
        // But if only 2 points are provided (as in this case), then these points will lie on the rectangular perimeter!
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                                    .include(locationLatLon)
                                                    .build();

        //Define the The padding of the 'bounds' perimeter.
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);


        CameraUpdate updateCamera = CameraUpdateFactory.newLatLngBounds(latLngBounds, margin);

        mMap.animateCamera(updateCamera);


    }
}
