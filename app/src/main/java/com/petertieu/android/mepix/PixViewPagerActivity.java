package com.petertieu.android.mepix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Created by Peter Tieu on 13/12/2017.
 */

public class PixViewPagerActivity extends AppCompatActivity{

    private static final String TAG = "PixViewPagerActivity";
    private static final String EXTRA_PIX_ID = "com.petertieu.android.mepix";
    private static final int OFF_SCREEN_PAGE_LIMIT = 5;


    private static ViewPager mViewPager;
    private List<Pix> mPixes;


    public static Intent newIntent(Context context, UUID pixId){

        Intent intent = new Intent(context, PixViewPagerActivity.class);

        intent.putExtra(EXTRA_PIX_ID, pixId);

        return intent;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate(..) called");

        setContentView(R.layout.activity_pix_view_pager);

        mViewPager = (ViewPager) findViewById(R.id.pix_view_pager);

        mViewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);

        mPixes = PixManager.get(this).getPixes();




    }


}
