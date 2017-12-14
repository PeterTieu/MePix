package com.petertieu.android.mepix;

import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Peter Tieu on 7/12/2017.
 */



//Activity hosting PixListFragment
public class PixListActivity extends SingleFragmentActivity implements PixListFragment.Callbacks{


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



    @Override
    public void onNewPix(Pix pix){

        //If the two-pane view does NOT exist...
        if (findViewById(R.id.detail_fragment_container) == null){
            Intent PixViewPager = PixViewPagerActivity.newIntent(this, pix.getId());
        }
        //If the two-pane view EXISTS
        else{
            //
        }



    }








}
