package com.petertieu.android.mepix;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Peter Tieu on 7/12/2017.
 */



//Activity hosting PixListFragment
public class PixListActivity extends SingleFragmentActivity implements PixListFragment.Callbacks{


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
        return R.layout.activity_masterdetail;
    }



    //Override the method from the PixDetailFragment.Callbacks interface
    @Override
    public void onPixSelected(Pix pix){

        //Log lifecycle callback
        Log.i(TAG, "newIntent(..) onPixSelected(..) called");

        //If the two-pane view does NOT exist... i.e. sw < 600dp
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
