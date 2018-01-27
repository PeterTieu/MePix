package com.petertieu.android.mepix;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;




//Activity hosting PixListFragment
public class PixListActivity extends OneFragmentActivity implements PixListFragment.Callbacks, PixDetailFragment.Callbacks{

    //Declare tag for Logcat
    private static final String TAG = "PixListActivity";

    //Declare instance variable.
    // REQUEST_ERRORS is a requestCode given when calling startActivityForResult.
    private static final int REQUEST_ERROR = 0;

    //Declare boolean flag to be triggered when the two-pane mode has been entered (i.e. when sw > 600dp).
    //This is so that the menu items from PixDetailFragment ('delete' button) and PixListFragment ('New Pix' button) don't get combined
    //in the same toolbar when the user rotates the screen from two-pane layout to one-pane layout!
    public static boolean hasEnteredTwoPaneMode;


    //Override the abstract method from OneFragmentActivity
    @Override
    protected Fragment createFragment(){
        //Return the fragment, PixListFragment
        return new PixListFragment();
    }




    //Override the abstract method from OneFragmentActivity
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

        //If the 2nd pane does NOT exist.. in other words, if two-pane view does NOT exist... i.e. sw < 600dp
        if (findViewById(R.id.detail_fragment_container) == null){

            //Declare boolean two-pane mode flag as FALSE
            hasEnteredTwoPaneMode = false;

            //Create an Intent to start the PixViewPagerActivity activity
            Intent PixViewPagerIntent = PixViewPagerActivity.newIntent(this, pix.getId());

            //Start the Intent
            startActivity(PixViewPagerIntent);
        }

        //If the two-pane view EXISTS... i.e. sw > 600dp
        else{

            //Declare boolean two-pane mode flag as TRUE
            hasEnteredTwoPaneMode = true;

            //Create the PixDetailFragment fragment for the 2nd pane
            Fragment newPixDetailFragment = PixDetailFragment.newInstance(pix.getId());

            //Replace the 2nd pane with the PixDetailFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container, newPixDetailFragment).commit();
        }
    }




    //Override method from PixDetailFragment.Callbacks interface to update PixListFragment in real-time (for two-pane view)
    @Override
    public void onPixUpdatedFromDetailView(Pix pix){

        //Log lifecycle callback method in Logcat
        Log.i(TAG, "PixDetailFragment.Callbacks onPixUpdatedFromDetailView(..) called");

        //Get PixListFragment from SupportFragmentManager
        PixListFragment pixListFragment = (PixListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        //Update PixListFragment
        pixListFragment.updateUI();
    }





    //Override method from PixDetailFragment.Callbacks interface to delete Pix from PixDetailFragment in real-time (for two-pane view)
    @Override
    public void onPixDeleted(Pix pix){

        //Log lifecycle callback method in Logcat
        Log.i(TAG, "PixDetailFragment.Callbacks onPixDeleted(..) called");

        //Get PixDetailFragment from SupportFragmentManager
        PixDetailFragment pixDetailFragment = (PixDetailFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment_container);

        //Remove PixDetailFragment from 2nd pane
        getSupportFragmentManager().beginTransaction().remove(pixDetailFragment).commit();
    }





    //Override method from PixListFragment.Callbacks interface to update PixDetailFragment in real-time (for two-pane view)
    @Override
    public void onPixUpdatedFromListView(Pix pix){

        //Log lifecycle callback method in Logcat
        Log.i(TAG, "PixListFragment.Callbacks onPixUpdatedFromListView(..) called");

        //If the 2nd pane does NOT exist.. in other words, if two-pane view does NOT exist... i.e. sw < 600dp
        if (findViewById(R.id.detail_fragment_container) == null){

            hasEnteredTwoPaneMode = false;

            //Create an Intent to start the PixViewPagerActivity activity
            Intent PixViewPagerIntent = PixViewPagerActivity.newIntent(this, pix.getId());

            //Start the Intent
            startActivity(PixViewPagerIntent);
        }

        //If the two-pane view EXISTS... i.e. sw > 600dp
        else{

            hasEnteredTwoPaneMode = true;

            //Create the PixDetailFragment fragment for the 2nd pane
            Fragment newPixDetailFragment = PixDetailFragment.newInstance(pix.getId());

            //Replace the 2nd pane with the PixDetailFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container, newPixDetailFragment).commit();
        }
    }





    //Override activity lifecycle callback method
    @Override
    protected void onResume(){

        super.onResume();

        //Check if the Google Play Services are available (every time the app is opened, i.e., when onResume() is called)
        // This check is important, as the Play Services library is not always guaranteed to be working.
        //Create an instance of GoogleApiAvailability.
        // The GoogleApiAvailability class extends Object.
        // It is a Helper class for verifying that the Google Play services APK is available and up-to-date on the device.
        //getInstance() is static method from GoogleApiAvailability. Returns a GoogleApiAvailability object
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();


        //Check if the Google Play Services installed and enabled.
        //isGooglePlayServicesAvailable(Context) returns a status code (of type int).
        // IF Google Play Services is installed and enabled,
        // statusCode = SUCCESS.
        // IF not
        // statusCode = SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, or SERVICE INVALID.
        int statusCode = apiAvailability.isGooglePlayServicesAvailable(this);


        //If the Google Play Services is not available (i.e. not installed OR is disabled).
        // IOW, the status code from isGooglePlayServicesAvailable(..) does not return SUCCESS,
        // i.e., it returns" SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, or SERVICE INVALID.
        if (statusCode != ConnectionResult.SUCCESS){

            //getErrorDialog(Activity activity, int statusCode, int requestCode, DialogInterface.OnCancelListener cancelListener)
            // is from GoogleApiAvailbility class.
            // Returns a dialog to inform of the provided statusCode.
            // The returned dialog displays a localized message about the error and upon user confirmation (by tapping on dialog)
            // will direct them to the Play Store if Google Play services is out of date or missing,
            // or to system settings if Google Play services is disabled on the device.
            //Argument 1 (Activity): The parent activity for creating the dialog
            //Argument 2 (int): The status code of where Google Play Services is available. In this case, it would be !SUCCESS
            // i.e. SERVICE_MISSING, SERVICE_UPDATING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, or SERVICE INVALID.
            //Argument 3 (int): The request code given when calling startActivityForResult()
            //Argument 4 (DialogInterface.OnCancelListener): An inner class of DialogInterface that contains the onCancelListener
            // (i.e what happens when the cancel button of the Dialog is pressed)
            Dialog errorDialog = apiAvailability.getErrorDialog(this, statusCode, REQUEST_ERROR, new DialogInterface.OnCancelListener() {


                //Override the onCancel(DialogInterface) method of the DialogInterface.OnCallListener interface.
                //It is called when the dialog is canceled.
                @Override
                public void onCancel(DialogInterface dialogInterface) {

                    //finish() is from Activity class.
                    //Leave app if services are unavailable
                    finish();

                }
            });
        }
    }






}
