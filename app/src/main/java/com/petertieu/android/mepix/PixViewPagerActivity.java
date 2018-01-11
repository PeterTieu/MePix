package com.petertieu.android.mepix;

;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.UUID;

/**
 * Created by Peter Tieu on 13/12/2017.
 */

//Activity hosting PixDetailFragment(s)
//NOTE: PixDetailFragment.Callbacks interface is implemented because PixDetailFragment, i.e. the fragment class being hosted, declares this interface (mandatory)
public class PixViewPagerActivity extends AppCompatActivity implements PixDetailFragment.Callbacks{

    //Define log identifier
    private static final String TAG = "PixViewPagerActivity";

    //Define 'key' for the Intent 'value'
    private static final String EXTRA_PIX_ID = "com.petertieu.android.mepix";

    //Define total number of fragments to pre-load outside of the fragment on screen
    private static final int OFF_SCREEN_PAGE_LIMIT = 5;

    //Declare ViewPager
    private static ViewPager mViewPager;

    //Declare List of Pix objects
    private List<Pix> mPixes;



    //Mandatory override of callback method from the 'PixDetailFragment.Callbacks' callback interface
    @Override
    public void onPixUpdatedFromDetailView(Pix pix){
        //Do nothing
    }

    @Override
    public void onPixDeleted(Pix pix){
        //Do nothing
    }



    //Method to call for
    public static Intent newIntent(Context context, UUID pixId){

        //Log to Logcat
        Log.i(TAG, "newIntent(..) called");

        //Create new intent to start PixViewPagerActivity
        Intent intent = new Intent(context, PixViewPagerActivity.class);

        //Add extra to intent
        intent.putExtra(EXTRA_PIX_ID, pixId);

        //Return the Intent
        return intent;
    }





    //Override onCreate(..) activity lifecycle callback method
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Log to Logcat
        Log.i(TAG, "onCreate(..) called");

        //Set the activity content from the ViewPager layout resource
        setContentView(R.layout.activity_pix_view_pager);

        //Assign the ViwePager to its associated resource ID
        mViewPager = (ViewPager) findViewById(R.id.pix_view_pager);

        //Set total number of detail fragments to pre-load outside of the current fragment on screen
        mViewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);

        //Assign the Pix instance reference variable to the PixManager singleton
        mPixes = PixManager.get(this).getPixes();

        //Create a FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();

        //Log size of mPixes to Logcat
        Log.i(TAG, "mPixes.size() = " + mPixes.size());

        //Set the Adapter to the ViewPager
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            //Override method from the FragmentStatePagerAdapter
            @Override
            public Fragment getItem(int position) {

                //Get a specific Pix from the List of Pix objects
                Pix pix = mPixes.get(position);

                //Create and return a new PixDetailFragment fragment
                return PixDetailFragment.newInstance(pix.getId());
            }

            //Override method from the FragmentStatePagerAdapter
            @Override
            public int getCount() {

                Log.i(TAG, "3");
                //Get the size of the List of Pix objects
                return mPixes.size();
            }
        });


        //Get the 'value' associated with the 'key' from the Intent that started this activity
        UUID pixId = (UUID) getIntent().getSerializableExtra(EXTRA_PIX_ID);

        Log.i(TAG, "4");

        //Display the detail view of the Pix that was clicked on in the list view
        for(int i=0; i<mPixes.size(); i++){

            //If the current Pix object from the List of Pix objects has the same UUID as the one clicked on in the list iew
            if (mPixes.get(i).getId().equals(pixId)){

                //Set detail view to display this Pix
                mViewPager.setCurrentItem(i);

                break;
            }
        }





    }


}
