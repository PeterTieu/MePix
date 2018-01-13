package com.petertieu.android.mepix;
import android.app.IntentService;
import android.app.IntentService;
import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.petertieu.android.mepix.FetchAddressIntentService.Constants.FAILURE_RESULT;
import static com.petertieu.android.mepix.FetchAddressIntentService.Constants.SUCCESS_RESULT;
import static com.petertieu.android.mepix.FetchAddressIntentService.Constants.TAG;


/**
 * Created by Peter Tieu on 12/01/2018.
 */



//Address lookup service - uses REVERSE GEOCODING to conver geographic location (i.e. lat/lon points) to address
//Worker thread - Operates ASYNCHRONOUSLY from Main UI thread

//WHAT IS A SERVICE?
//A Service is an application component that can perform long-running operations in the BACKGROUND, and does not provide a UI.
//Another application component can start a service, and continues to run in the background even if the user switches to another app.
//A service can allow off-the-screen operations, e.g. play music while on standby, check the status bar for notifications

//NOTE: Because Services respond to intents in the same way that activities do, they MUST also be DECLARED in the Manifest!

//NOTE: IntenService will shut down after all intents are processed
public class FetchAddressIntentService extends IntentService {

    //Define constants
    public final class Constants {
        public static final String TAG = "FetchAddressIntServ"; //Tag for Logcat
        public static final int SUCCESS_RESULT = 102; //Indicate a success
        public static final int FAILURE_RESULT = 101; //Indicate a failure
        public static final String PACKAGE_NAME = "com.petertieu.android.mepix"; //Package name
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER"; //Receiver extra of Intent
        public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY"; //'Key' for result data
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA"; //Location extra of Intent
    }

    //Declare ResultReceiver
    protected ResultReceiver mReceiver;

    //Build constructor
    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }


    //Handle data (ResultReceiver object and Location object) sent to this IntentService
    @Override
    protected void onHandleIntent(Intent intent) {

        //Extract ResultReceiver from PixDetailFragment. NOTE: ResultReceiver is where the address data from reverse geocoding will be sent to
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        //Extract Location from PixDetailFragment
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        //Declare error message variable
        String errorMessage = "";


        //If ResultReceiver does NOT exist
        if (mReceiver == null) {
            //Log result
            Log.e(TAG, "AdddressResultReceiver was sent to FetchAddressIntentService - there is no ResultReceiver for this IntentService to send data to.");
            return;
        }

        //Create Geocoder to perform reverse geocoding. NOTE: Locale object represents the linguistic presentation of the address information specific to the region
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());


        //If location does NOT exist
        if (location == null) {

            //Define error message
            errorMessage = "Location does not exist - no location specified.";

            //Send 'negative' results to ResultReceiver from PixDetailFragment
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);

            return;
        }


        //If Geocoder EXISTS
        if (geocoder.isPresent()) {

            //Declare list to hold all representations of the address (e.g. address line, city, state, postal code, country, etc.) from reverse geocoding
            List<Address> addressRepresentations = null;


            //Try 'risky' task. getFromLocation(..) could throw IOException or IllegalArgumentException
            try {
                //Get address from Geocoder
                addressRepresentations = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            }
            //Catch network or other I/O problems
            catch (IOException ioException){
                //Print stack trace
                ioException.printStackTrace();
            }
            //Catch invalid latitude or longitude values
            catch (IllegalArgumentException illegalArgumentException){
                //Log error message
                Log.e(TAG, errorMessage + ". " + "Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude(), illegalArgumentException);
            }


            //If Geocoder exists BUT address does NOT exist
            if (addressRepresentations == null || addressRepresentations.size() == 0) {

                //Update error message
                errorMessage = "No address found at location.";

                //Send 'negative' results back to PixDetailFragment
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            }

            //If Geocoder exists AND address line is found
            else{
                //Get address
                String addressLine = addressRepresentations.get(0).getAddressLine(0);
                String featureName = addressRepresentations.get(0).getFeatureName();
                String city = addressRepresentations.get(0).getLocality();
                String state = addressRepresentations.get(0).getAdminArea();
                String postalCode = addressRepresentations.get(0).getPostalCode();

                //If address line exists
                if (addressLine != null) {
                    Log.i(TAG, "Address: " + addressLine);

                    //Send 'positive' results back to PixDetailFragment
                    deliverResultToReceiver(Constants.SUCCESS_RESULT, addressLine);
                }

                //If feature name does NOT exist
                else{
                    Log.i(TAG, "Get feature name: " + featureName);

                    //Send 'positive' results back to PixDetailFragment
                    deliverResultToReceiver(Constants.SUCCESS_RESULT, featureName);
                }

            }

        }

        //If Geocoder is NOT present
        else{
            //Update error message
            errorMessage = "No geocoder present.";

            //Send 'negatibe' results back to PixDetailFragment
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        }

    }


    //Send results back to PixDetailFragment
    private void deliverResultToReceiver(int resultCode, String message) {

        //Create Bundle object to use as argument-bundle
        Bundle bundle = new Bundle();

        //Stash result key-value pair to argument-bundle
        bundle.putString(Constants.RESULT_DATA_KEY, message);

        //Send argument-bundle to ResultReceiver (in this case, AddressResultReceiver from PixDetailFragment)
        mReceiver.send(resultCode, bundle);
    }
}
