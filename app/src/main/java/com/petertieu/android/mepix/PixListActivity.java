package com.petertieu.android.mepix;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Peter Tieu on 7/12/2017.
 */



//Activity hosting PixListFragment
public class PixListActivity extends SingleFragmentActivity implements PixListFragment.Callbacks, PixDetailFragment.Callbacks{

    //Declare tag for Logcat
    private static final String TAG = "PixListActivity";


    //Override the abstract method from SingleFragmentActivity
    @Override
    protected Fragment createFragment(){
        //Return the fragment, PixListFragment
        return new PixListFragment();
    }




    //Override the abstract method from SingleFragmentActivity
    @Override
    protected int getLayoutResId(){

        //If no pixes exist (regardless of screen width or smallest screen width value)
        if (PixManager.get(this).getPixes().size() == 0){
            //Return single fragment view
            return R.layout.activity_fragment;
        }

        //If one or more pixes exist, return ref.xml file, which is dependent on screen width and smallest screen width
        return R.layout.activity_masterdetail;
    }





    //Override method from PixListFragment.Callbacks interface
    @Override
    public void onPixSelected(Pix pix){

        //Log callback method
        Log.i(TAG, "PixListFragment.Callbacks onPixSelected(..) called");

        //If the 2nd pane doesn NOT exist.. in other words, if two-pane view does NOT exist... i.e. sw < 600dp
        if (findViewById(R.id.detail_fragment_container) == null){

            //Create an Intent to start the PixViewPagerActivity activity
            Intent PixViewPagerIntent = PixViewPagerActivity.newIntent(this, pix.getId());

            //Start the Intent
            startActivity(PixViewPagerIntent);
        }

        //If the two-pane view EXISTS... i.e. sw > 600dp
        else{

            //Create the PixDetailFragment fragment for the 2nd pane
            Fragment newPixDetailFragment = PixDetailFragment.newInstance(pix.getId());

            //Replace the 2nd pane with the PixDetailFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container, newPixDetailFragment).commit();
        }
    }




    //Override method from PixDetailFragment.Callbacks interface to update PixListFragment in realtime (for two-pane view)
    @Override
    public void onPixUpdatedFromDetailView(Pix pix){

        //Log lifecycle callback method in Logcat
        Log.i(TAG, "PixDetailFragment.Callbacks onPixUpdatedFromDetailView(..) called");

        //Get PixListFragment from SupportFragmentManager
        PixListFragment pixListFragment = (PixListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        //Update PixListFragment
        pixListFragment.updateUI();
    }





    //Override method from PixDetailFragment.Callbacks interface to delete Pix from PixDetailFragment in realtime (for two-pane view)
    @Override
    public void onPixDeleted(Pix pix){

        //Log lifecycle callback method in Logcat
        Log.i(TAG, "PixDetailFragment.Callbacks onPixDeleted(..) called");

        //Get PixDetailFragment from SupportFragmentManager
        PixDetailFragment pixDetailFragment = (PixDetailFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);

        //Remove PixDetailFragment from 2nd pane
        getSupportFragmentManager().beginTransaction().remove(pixDetailFragment).commit();
    }






    //Override method from PixListFragment.Callbacks interface to update PixDetailFragment in realtime (for two-pane view)
    @Override
    public void onPixUpdatedFromListView(Pix pix){

        //Log lifecycle callback method in Logcat
        Log.i(TAG, "PixListFragment.Callbacks onPixUpdatedFromListView(..) called");

        //If the 2nd pane doesn NOT exist.. in other words, if two-pane view does NOT exist... i.e. sw < 600dp
        if (findViewById(R.id.detail_fragment_container) == null){

            //Create an Intent to start the PixViewPagerActivity activity
            Intent PixViewPagerIntent = PixViewPagerActivity.newIntent(this, pix.getId());

            //Start the Intent
            startActivity(PixViewPagerIntent);
        }

        //If the two-pane view EXISTS... i.e. sw > 600dp
        else{

            //Create the PixDetailFragment fragment for the 2nd pane
            Fragment newPixDetailFragment = PixDetailFragment.newInstance(pix.getId());

            //Replace the 2nd pane with the PixDetailFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container, newPixDetailFragment).commit();
        }
    }






}
