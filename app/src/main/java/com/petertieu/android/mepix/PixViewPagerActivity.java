package com.petertieu.android.mepix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.util.List;
import java.util.UUID;




//Activity hosting PixDetailFragment(s)
//NOTE: PixDetailFragment.Callbacks interface MUST be implemented because PixDetailFragment, i.e. the fragment class being hosted,
// declares this callback interface in its class
public class PixViewPagerActivity extends AppCompatActivity implements PixDetailFragment.Callbacks{

    //Define log identifier
    private static final String TAG = "PixViewPagerActivity";

    //Define 'key' for the Intent 'value'
    private static final String EXTRA_PIX_ID = "com.petertieu.android.mepix";

    //Define total number of fragments to pre-load outside of the fragment on screen
    private static final int OFF_SCREEN_PAGE_LIMIT = 5;

    //Declare static ViewPager
    public static ViewPager mViewPager;

    //Declare List of Pix objects
    private List<Pix> mPixes;




    //Mandatory override of callback method from the 'PixDetailFragment.Callbacks' callback interface
    @Override
    public void onPixUpdatedFromDetailView(Pix pix){
        //Do nothing
    }





    //Manadatory override of callback method from the 'PixDetailFragment.Callbacks' callbacks interface
    @Override
    public void onPixDeleted(Pix pix){
        //Do nothing
    }





    //Method to be called in order to begin this activity
    //NOTE: This method can be called by these callback interfaces from PixListFragment: onPixSelected(Pix) and onPixUpdatedFromListView(Pix),
    // which are defined in PixListActivity.
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

        //Assign the ViewPager to its associated resource ID
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
                //Get the size of the List of Pix objects
                return mPixes.size();
            }
        });


        //Get the 'value' associated with the 'key' from the Intent that started this activity
        UUID pixId = (UUID) getIntent().getSerializableExtra(EXTRA_PIX_ID);

        Log.i(TAG, "4");


        //Set the current number of the Pix so that the ViewPager knows which Pix number is being displayed.
        //Ultimately, there would be smooth transition between Pixes
        for(int i=0; i<mPixes.size(); i++){

            //If the current Pix object from the List of Pix objects has the same UUID as the one clicked on in the list iew
            //NOTE: The get(..) here isn't the same as the get(..) from PixManager! It is from the List interface!
            if (mPixes.get(i).getId().equals(pixId)){

                //Set detail view to display this Pix
                mViewPager.setCurrentItem(i);

                break;
            }
        }
    }





    //Override onResume() fragment lifecycle callback method
    @Override
    public void onResume(){
        super.onResume();

        //Log fragment activity lifecycle event in Logcat
        Log.i(TAG, "onResume() called");

        //Set the ViewPagerActivity tracker's visibility instance variable as TRUE
        PixViewPagerActivityLifecycleTracker.activityResumed();
    }





    //Override onPause() fragment lifecycle callback method
    @Override
    public void onPause(){
        super.onPause();

        //Log fragment activity lifecycle event in Logcat
        Log.i(TAG, "onPause() called");

        //Set the ViewPagerActivity tracker's visibility instance variable as FALSE
        PixViewPagerActivityLifecycleTracker.activityPaused();
    }





    //Override onStop() fragment lifecycle callback method
    @Override
    public void onStop(){
        super.onStop();

        //Log fragment activity lifecycle event in Logcat
        Log.i(TAG, "onStop() called");
    }





    //Override onDestroy() fragment lifecycle callback method
    @Override
    public void onDestroy(){
        super.onDestroy();

        //Log fragment activity lifecycle event in Logcat
        Log.i(TAG, "onDestroy() called");
    }

}
