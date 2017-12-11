package com.petertieu.android.mepix;

import android.support.v4.app.Fragment;

/**
 * Created by Peter Tieu on 7/12/2017.
 */



//Activity hosting PixListFragment
public class PixListActivity extends SingleFragmentActivity {


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





}
