package com.petertieu.android.mepix;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.text.format.DateFormat;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.Context.LOCATION_SERVICE;


/**
 * Created by Peter Tieu on 14/12/2017.
 */





//Fragment for the DETAIL VIEW
public class PixDetailFragment extends Fragment{

    //Define 'key' for the argument-bundle
    private static final String ARGUMENT_PIX_ID = "pix_id";

    //Define TAG for Logcat
    private static final String TAG = "PixDetailFragment";

    //Declare Pix instance variable
    private Pix mPix;

    //Declare View instance variables
    private EditText mTitle;    //Title
    private CheckBox mFavoritedButton; //Favorited Button
    private Button mDateButton; //Date Button
    private Button mTagButton; //Tag Button
    private EditText mTagEditText; //Tag EditText
    private String mTotalTag;
    private Button mLocationButton; //Location Button
    private EditText mDescription; //Description Button
    private ImageButton mPictureAddButton; //Photo Button
    private File mPictureFile; //Picture File
    private ImageView mPictureView; //Picture ImageView

    //Declare DateFormat object for formatting the date display
    private DateFormat mDateFormat;

    //Declare identifiers for dialog fragments
    private static final String IDENTIFIER_DIALOG_FRAGMENT_DATE = "DialogDate"; //Identifier of dialog fragment of DatePicker
    private static final String IDENTIFIER_DIALOG_FRAGMENT_PICTURE = "IdentifierDialogFragmentPicture"; //Identifier of dialog fragment of picture ImageView
    private static final String IDENTIFIER_DIALOG_FRAGMENT_DELETE_CONFIRMATION = "DialogDeleteConfirmation"; //Identifier of dialog fragment
    private static final String IDENTIFIER_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION ="DialogUpdateLocationConfirmation";

    //Declare constants for tag requests
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_DATE = 0;  //Request code for receiving results from dialog fragment
    private static final int REQUEST_CODE_CONTACT = 1; //Request code for results returned from contact activity/app
    private static final int REQUEST_CODE_PICTURE_CAMERA = 2; //Request code for results returned from camera activity/app
    private static final int REQUEST_CODE_PICTURE_GALLERY = 3;
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_DELETE_CONFIRMATION = 10; //Request code for receiving results from dialog fragment
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION = 11;
    private static final int REQUEST_CODE_FOR_LOCATION_PERMISSIONS = 4; //Request code for location fix
    private static final int REQUEST_CODE_FOR_STORAGE_PERMISSIONS = 1; //Request code to WRITE to external storage
    private static final int REQUEST_CODE_NEW_MARKER_LOCATION = 5;
    private static final int REQUEST_CODE_FOR_WRITE_EXTERNAL_STORAGE_PERMISSION = 6;

    //Declare Callbacks interface reference variable
    private Callbacks mCallbacks;

    //Declare FusedLocationProviderClient - the entry point for interacting with FusedLocationProvider (to get location fix)
    private FusedLocationProviderClient mFusedLocationClient;

    //List locations permissions required,
    private static final String[] LOCATION_PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final String[] STORAGE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};



    //Declare Location object to contain location fix (i.e lat/lon values)
    protected Location mLocation;

    private Address mAddressOutput;

    //Declare AddressResultReceiver object, which is a ResultReceiver - to receive results from reverse geocoding
    private AddressResultReceiver mAddressResultReceiver;

    private boolean updatePixLocationMenuItemPressed = false;


    AddressResultReceiver mAddressResultReceiverr;

    protected Location mNewLocation;
    private AddressResultReceiver mNewAddressResultReceiver;

    private boolean locationSavedToDB = false;


    //Declare a GoogleApiClient reference variable.
    //NOTE: To use the Google Play Services, we must CREATE a CLIENT (GoogleApiClient).
    private GoogleApiClient mGoogleApiClient;

    //Declare GoogleMap
    // A GoogleMap is the main class for the Google Maps Android API.
    // It is the ENTRY POINT for all methods related to the map.
    //A GoogleMap is already instantiated in SupportMapFragment.
    // It must be obtained from getMapAsync(OnMapReadyCallback) (a method from the SupportMapFragment class).
    private GoogleMap mMap;





    //Build 'encapsulating' constructor
    public static PixDetailFragment newInstance(UUID pixId){

        //Create Bundle object (i.e. argument-bundle)
        Bundle argumentBundle = new Bundle();

        //Set key-value pairs for the argument bundle
        argumentBundle.putSerializable(ARGUMENT_PIX_ID, pixId);

        //Create PixDetailFragment
        PixDetailFragment pixDetailFragment = new PixDetailFragment();

        //Set argument-bundle for the PixDetailFragment
        pixDetailFragment.setArguments(argumentBundle);

        //Return PixDetailFragment
        return pixDetailFragment;

    }





    //Declare callback interface
    interface Callbacks{

        //Callback method for when a Pix's instance variable is changed (for two-pane layout)
        void onPixUpdatedFromDetailView(Pix pix);

        //Callback method for when a Pix is deleted (for two-pane layout)
        void onPixDeleted(Pix pix);
    }





    //Override onAttach() fragment lifecycle callback method
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        //Log in Logcat
        Log.i(TAG, "onAttach() called");

        //Declare mCallbacks ref. var. (to PixDetailFragment.Callbacks)
        mCallbacks = (Callbacks) context;
    }





    //Override onCreate(..) fragment lifecycle callback method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log in Logcat
        Log.i(TAG, "onCreate(..) called");

        //Get the 'value' of the argument-bundle
        UUID pixId = (UUID) getArguments().getSerializable(ARGUMENT_PIX_ID);

        //Create Pix object
        mPix = new Pix();

        //Assign Pix object to Pix object from PixManager singleton
        mPix = PixManager.get(getActivity()).getPix(pixId);

        //Declare an options menu for the fragment
        setHasOptionsMenu(true);

        //If the user has previously entered two-pane mode (i.e. sw > 600dp), and it now no longer exists,
        // since the user has rotated the screen, and is now back to one-pane mode (i.e. is now in the list view)...
        if (PixListActivity.hasEnteredTwoPaneMode == true && getActivity().findViewById(R.id.detail_fragment_container) == null) {
            //Disable the options menu of this detail view, which effectively removes the 'Delete' button from the toolbar while in list view
            setHasOptionsMenu(false);
        }

        //Assign reference variable, mPictureFile, to picture file in FileProvider
        mPictureFile = PixManager.get(getActivity()).getPictureFile(mPix);



        //========== Request for Write-to-external-storage (dangerous) permission ====================================================

        if (hasWriteExternalStoragePermission() == false){
            requestPermissions(STORAGE_PERMISSIONS, REQUEST_CODE_FOR_STORAGE_PERMISSIONS);
        }



        //========== Request for Location (dangerous) permissions (from Location permission group) ====================================================

        //Create AddressResultReceiver object, passing a Handler to it
        mAddressResultReceiver = new AddressResultReceiver(new Handler());

        //If location permissions requested in the Manifest have NOT been granted
        if (hasLocationPermission() == false) {

            //Request (user) for location permissions - as they are 'dangerous' permissions
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_CODE_FOR_LOCATION_PERMISSIONS);
        }

        //If location permissions requested in the Manifest HAVE been granted
        else {
            //========== Configure location services ====================================================
            //Create FusedLocationProviderClient object - to get location fix
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            //Try risky task - addSuccessListener could throw a SecurityException exception
            try {
                //Get most recent location available via getLastLocation(). NOTE: getLastLocation() runs ASYNCHRONOUSLY, whereas .
                mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {

                    //Listen for when location has been obtained via FusedLocationClient
                    @Override
                    public void onSuccess(Location location) {
                        //If location does not
                        if (location == null) {
                            Log.v(TAG, "Location does not exist");
                        }

                        //Stash mLocation instance reference variable to local reference variable of location object
                        mLocation = location;

                        //In some rare cases, location returned can be null
                        if (mLocation == null) {
                            return;
                        }

                        //If Geocoder is NOT present (Geocoder: object to convert location fix (lat/lon values) to linguistic address
                        if (!Geocoder.isPresent()) {
                            Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                            return;
                        }

                        //Start IntentService to perform reverse geocoding (i.e. obtaining address from location fix (i.e. lat/lon values))
                        startIntentService();
                    }
                });
            }
            //Catch any SecurityException thrown by addSuccessListener(..)
            catch (SecurityException securityException) {
                Log.e(TAG, "Error creating location service", securityException);
            }
        }





        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                //If updated location does not
                if (locationResult == null) {
                    Log.v(TAG, "Updated location does not exist");
                }

                mLocation = locationResult.getLastLocation();

                //In some rare cases, location returned can be null
                if (mLocation == null) {
                    return;
                }

                //If Geocoder is NOT present (Geocoder: object to convert location fix (lat/lon values) to linguistic address
                if (!Geocoder.isPresent()) {
                    Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                    return;
                }


                mAddressResultReceiver = new AddressResultReceiver(new Handler());

                //Start IntentService to perform reverse geocoding (i.e. obtaining address from location fix (i.e. lat/lon values))
                startIntentService();

            }
        };



    }







    //======================== Define ADDRESS services helper methods =============================================

    //Check if location permission requested in the Manifest has been granted
    private boolean hasLocationPermission(){

        //If permission is granted, result = PackageManager.PERMISSION_GRANTED (else, PackageManager.PERMISSION_DENIED).
        //NOTE: Permissions ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION are in the same PERMISSION GROUP, called ADDRESS.
            //If one permission is a permission group is granted/denied access, the same applies to all other permissions in that group.
            // Other groups include: CALENDAR, CAMERA, CONTACTS, MICROPHONE, PHONE, SENSORS, SMS, STORAGE.
        int result = ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);

        //Return a boolean for state of location permission
        return (result == PackageManager.PERMISSION_GRANTED);
    }


    private boolean checkNetworkAndLocationServicesAvailability(){
        LocationManager locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);

        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try{
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex){
        }

        try{
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch(Exception ex){
        }


        if (!gpsEnabled && !networkEnabled){
            Toast.makeText(getActivity(), "Location is not enabled", Toast.LENGTH_SHORT).show();
        }

        return gpsEnabled;
    }



    public boolean isGpsEnabled(){

        LocationManager service = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER)&&service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }







    private boolean hasWriteExternalStoragePermission(){
        int result = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (result == PackageManager.PERMISSION_GRANTED);
    }





    //Call IntentService to perform REVERSE GEOCODING (i.e. obtain address from location fix)
    protected void startIntentService() {

        //Create intent to FetchAddressIntentService
        Intent intentFetchAddressIntentService = new Intent(getActivity(), FetchAddressIntentService.class);

        //Pass Location which contains lat/lon data of location fix to convert to address
        intentFetchAddressIntentService.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLocation);

        //Pass AddressResultReceiver (ResultReceiver) to receive and handle address results from reverse geocoding
        intentFetchAddressIntentService.putExtra(FetchAddressIntentService.Constants.RECEIVER, mAddressResultReceiver);

        //Start FetchAddressIntentService (IntentService) to perform reverse geocoding
        getActivity().startService(intentFetchAddressIntentService);
    }





    //Define AddressResultReceiver (ResultReceiver) inner class - to receive results from FetchAddressIntentService (IntentService)
    class AddressResultReceiver extends ResultReceiver {

        //Build constructor
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        //Listen for results from FetchAddressIntentService (IntentService)
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            //Define address string OR error message sent from FetchAddressIntentService (IntentService)
            mAddressOutput = resultData.getParcelable(FetchAddressIntentService.Constants.RESULT_DATA_KEY);

            //If result code returned from IntentService yields 'negative' - address was not obtained from location fix
            if (resultCode == FetchAddressIntentService.Constants.FAILURE_RESULT){
//                Toast.makeText(getActivity(), "Address not found", Toast.LENGTH_SHORT).show();

                //Display error message sent from FetchAddressIntentService (IntentService)
                mLocationButton.setText(mPix.getAddress());

            }

            //If result code returned from IntentService yields 'positive' - address successfully obtained from location fix
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {

                //If mLocation field of Pix object does NOT exist (i.e. no address set yet) OR updatePixLocation MenuItem was pressed
                if ( (mPix.getAddress() == null) || (updatePixLocationMenuItemPressed == true) ){

                    //Set address to mAddress field of Pix object
                    mPix.setAddress(mAddressOutput.getAddressLine(0));

                    //Set locality component of the address to mLocality field of Pix object
                    mPix.setLocality(mAddressOutput.getLocality() + ", " + mAddressOutput.getAdminArea());

                    //Set latitude component of the address to mLatitude field of Pix object
                    mPix.setLatitude(mAddressOutput.getLatitude());

                    //Set longitude component of the address to mLongitude field of Pix object
                    mPix.setLongitude(mAddressOutput.getLongitude());

                    //Update SQLiteDatabase of Pix account for data added/updated for mLocation field (i.e. address)
                    updatePix();


                    //Display address on location button
                    mLocationButton.setText(mPix.getAddress());

                    //Reset the updatePixLocationItemPressed variable back to 'false'
                    updatePixLocationMenuItemPressed = false;
                }

                //If mLocation field of Pix object DOES exist (i.e. address has been set)
                else{
                    //Display address on location button
                    mLocationButton.setText(mPix.getAddress());
                }

            }
        }
    }





    //Override onCreateView(..) fragment lifecycle callback method
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle){
        super.onCreateView(layoutInflater, viewGroup, bundle);

        //Log in Logcat
        Log.i(TAG, "onCreateView(..) called");

        //Inflate the View for the fragment
        View view = layoutInflater.inflate(R.layout.fragment_pix_detail, viewGroup, false);



        //================ SET UP mTitle ==================================================================
        //Assign title instance variable to its associated resource ID
        mTitle = (EditText) view.findViewById(R.id.detail_pix_title);

        //Set text of the title to title instance variable of the Pix
        mTitle.setText(mPix.getTitle());

        //Add listener to text EditText
        mTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Set title of the Pix to the current String in the EditText
                mPix.setTitle(charSequence.toString());

                //Update the Pix
                updatePix();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Do nothing
            }
        });




        //================ SET UP mDateButton ==================================================================
        //Assign date EditText instance variable to its associated resource ID
        mDateButton = (Button) view.findViewById(R.id.detail_pix_date);

        //If a date exists for the Pix
        if (mPix.getDate() != null){
            //Set text of the date button to date of the Pix
            mDateButton.setText(mDateFormat.format("EEE d MMMM yyyy", mPix.getDate()));
        }


        //Set listener the Date button
        mDateButton.setOnClickListener(new View.OnClickListener(){

            //Override onClick(..) from View.OnClickListener
            @Override
            public void onClick(View view){
                //Create FragmentManager
                FragmentManager fragmentManager = getFragmentManager();

                //Create DatePickerFragment fragment
                DatePickerFragment datePickerDialog = DatePickerFragment.newInstance(mPix.getDate());

                //Start the dialog fragment
                datePickerDialog.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_DIALOG_FRAGMENT_DATE);

                //Show dialog
                datePickerDialog.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_DATE);
            }
        });




        //================ SET UP mLocationButton ==================================================================


        //Assign location button instance variable to its associated resource ID
        mLocationButton = (Button) view.findViewById(R.id.detail_pix_location);


        if (mFusedLocationClient != null){

            if (mPix.getAddress() != null){
                mLocationButton.setText(mPix.getAddress());
            }
        }



        mLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {


                if(isGpsEnabled()) {
                    //Create Intent to open MapsActivity
                    Intent mapsActivityIntent = MapsActivity.newIntent(getActivity(), mPix.getId(), mPix.getLatitude(), mPix.getLongitude(), mPix.getAddress());

                    //Start intent to open MapsActivity
                    getActivity().startActivity(mapsActivityIntent);
                }

                else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setMessage("You need to turn on Location Services");
                    dialog.setPositiveButton(getContext().getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            getContext().startActivity(myIntent);
                            //get gps
                        }
                    });
                    dialog.setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub

                        }
                    });
                    dialog.show();
                }








            }
        });



        //================ SET UP mFavoritedButton ==================================================================
        //Assign favorited button instance variable to its associated resource ID
        mFavoritedButton = (CheckBox) view.findViewById(R.id.detail_pix_favorited);

        //Set sate of favorited button to its equivalent in Pix object
        mFavoritedButton.setChecked(mPix.isFavorited());

        //Set listener for CheckBox
        mFavoritedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            //Override onCheckedChanged(..) from CompoundButton.OnCheckedChangedListener
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Set favorited field of Pix to state of CheckBox
                mPix.setFavorited(isChecked);

                //Update Pix (upon new favorited change)
                updatePix();
            }
        });




        //================ SET UP mFavoritedButton ==================================================================
        //Assign description EditText instance variable to its associated resource ID
        mDescription = (EditText) view.findViewById(R.id.detail_pix_description);

        //Set text of the title to description instance variable of the Pix
        mDescription.setText(mPix.getDescription());

        //Add listener to description EditText
        mDescription.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Set description of the Pix to the current String in the EditText
                mPix.setDescription(charSequence.toString());

                //Update the Pix
                updatePix();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Do nothing
            }
        });




        //================ SET UP mTagButton ==================================================================
        //Assign tag Button instance variable to its associated resource ID
        mTagButton = (Button) view.findViewById(R.id.detail_pix_tag);

        //Create implicit Intent to open contacts app to search for a contact (via ContactsContract.Contacts.CONTENT_URI), with the action of picking it
        final Intent pickTagIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        //Set listener for tag button
        mTagButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //Start intent, requesting for result
                startActivityForResult(pickTagIntent, REQUEST_CODE_CONTACT);
            }
        });

        //Ensure contacts app exists in device - so as to prevent app from crashing. Create PackageManager
        PackageManager packageManager = getActivity().getPackageManager();

        //If PackageManager does not resolve contacts activity from pickTagIntent
        if (packageManager.resolveActivity(pickTagIntent, PackageManager.MATCH_DEFAULT_ONLY) == null){
            //Disable mTagButton
            mTagButton.setEnabled(false);
        }




        //================ SET UP mTagEditText ==================================================================
        //Assign tag EditText instance variable to its associated resource ID
        mTagEditText = (EditText) view.findViewById(R.id.detail_edit_pix_tag);

        //If Pix tag exists, display it in tag EditText
        if (mPix.getTag() != null){
            mTagEditText.setText(mPix.getTag());
        }

        //Set listener for tag EditText
        mTagEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Set String containing all tags from input
                mTotalTag = charSequence.toString();

                //Set Pix tag to String containing all tags from input
                mPix.setTag(mTotalTag);

                //Update Pix SQLiteDatabase and two-pane UI (upon changes in Pix tag EditText to tag field in Pix)
                updatePix();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Do nothing
            }
        });

        //Display Pix tag field in Pix tag EditText
        mTagEditText.setText(mPix.getTag());




        //================ SET UP mPhotoButton ==================================================================
        //Assign picture instance variable to its associated resource ID
        mPictureAddButton = (ImageButton) view.findViewById(R.id.detail_pix_add_picture);

        //Set listener for mPhotoButton
        mPictureAddButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                cameraIntent();

//                //Set dialog prompt items
//                final CharSequence[] dialogItems = {"Take Picture", "Choose from Gallery"};
//
//                //Create AlertDialog.Builder object
//                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
//
//                //Set title of dialog
//                alertDialogBuilder.setTitle("Add a Picture");
//
//                //Set items of dialog, and create listeners for them
//                alertDialogBuilder.setItems(dialogItems, new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int item) {
//
//                        //If "Take Picture" dialog item is pressed
//                        if (dialogItems[item].equals("Take Picture")) {
//
//                            //Log in Logcat
//                            Log.i(TAG, "*Take Photo* pressed");
//
//                            //Open camera activity/app (as new task)
//                            cameraIntent();
//                        }
//
//                        //If "Choose from Gallery" dialog item is pressed
//                        else if (dialogItems[item].equals("Choose from Gallery")) {
//
//                            //Log in Logcat
//                            Log.i(TAG, "*Choose from Library* pressed");
//
//                            //Open gallery activity/app (as new task)
//                            galleryIntent();
//                        }
//
////                        //If "Cancel" dialog item is pressed
////                        else if (dialogItems[item].equals("Cancel")) {
////
////                            //Log in Logcat
////                            Log.i(TAG, "*Cancel* pressed");
////
////                            //Close dialog
////                            dialog.dismiss();
////                        }
//                    }
//                });
//
//                //Set dialog 'Cancel' button
//                alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);
//
//                //Show dialog
//                alertDialogBuilder.show();
            }
        });




        //================ SET UP mPictureView ==================================================================
        //Assign picture view instance variable to its associated resource ID
        mPictureView = (ImageView) view.findViewById(R.id.detail_pix_picture);

        //Update picture view bitmap
        updatePictureView();

        //Set listener for picture view
        mPictureView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                //Check if picture File is empty. NOTE: A File exists for each Pix, but has length either >0 OR 0.
                // If File is not empty (i.e. contains .jpg), it has length > 0. If File is empty (i.e. does not contain .jpg), it has length 0.
                //NOTE: This check is important, as the app would crash when the ImageView is pressed on IF it has no .jpg (i.e. the Pix has an empty picture File)
                if (mPictureFile.length() != 0) {

                    //Open picture view dialog
                    ImageViewFragment pictureViewDialog = ImageViewFragment.newInstance(mPictureFile, mPix.getTitle(), mPix.getDate());

                    //Set PixDetailFragment as target fragment for the dialog fragment
                    pictureViewDialog.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_PICTURE_CAMERA);
//                    pictureViewDialog.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_PICTURE_GALLERY);

                    //Create FragmentManager (which has access to all fragments)
                    FragmentManager fragmentManager = getFragmentManager();

                    //Show the fragment
                    pictureViewDialog.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_PICTURE);

                }
            }
        });



        mViewToShare = (LinearLayout) view.findViewById(R.id.pix_detail_linear_layout);


        //Return the View
        return view;
    }




    //Update Pix SQLiteDatabase and two-pane UI (upon any changes)
    private void updatePix(){

        //Update the SQLite database based on the Pix passed
        PixManager.get(getActivity()).updatePixOnDatabase(mPix);

        //Update PixListFragment() in 'real-time' for two-pane layout
        mCallbacks.onPixUpdatedFromDetailView(mPix);
    }





    //Open camera activity/app
    private void cameraIntent(){

        //Create implicit intent with action to open camera activity/app
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Create PackageManager (which has access to all aps installed in the device)
        PackageManager packageManager = getActivity().getPackageManager();

        //Set boolean for condition: Picture file exists AND implicit intent can resolve camera activity via PackageManager
        boolean canTakePicture = (mPictureFile != null) && (takePictureIntent.resolveActivity(packageManager) != null);
        mPictureAddButton.setEnabled(canTakePicture);


        //Get content URI of FileProvider for which picture file from camera is to be saved to
        Uri uriFileProvider = FileProvider.getUriForFile(
                getActivity(), //(Context): The activity
                "com.petertieu.android.mepix.fileprovider", //(String): The authority of the FileProvider - defined in <provider> element in Manifest
                mPictureFile //(File): The picture file
        );

        //Add content URI as extra to the intent
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFileProvider);

        //Query PackageManager to obtain list of activities/apps that match conditions of captureImageIntent
        List<ResolveInfo> cameraActivities = packageManager.queryIntentActivities(
                takePictureIntent, //(Intent): The intent to open camera
                PackageManager.MATCH_DEFAULT_ONLY //Filter the query to only intents of the DEFAULT category
        );

        //Sort through all activities resolved (in the list)
        for (ResolveInfo resolvedActivity : cameraActivities){
            //Grant permsision for the resolved activity to write to the URI of the FileProvider
            getActivity().grantUriPermission(
                    resolvedActivity.activityInfo.packageName, //(String): The resolved activity
                    uriFileProvider, //(Uri): URI of FileProvider for which to grant access to
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION //(int): The access mode - allow writing to file
            );
        }

        //Start the activity, expecting results to be returned (via onActivityResult(..))
        startActivityForResult(takePictureIntent, REQUEST_CODE_PICTURE_CAMERA);
    }





    //Open gallery activity/app
    private void galleryIntent(){

        //Create implicit intent (to open gallery activity/app)
        Intent choosePictureIntent = new Intent();

        //Set action to open gallery activity/app
        choosePictureIntent.setAction(Intent.ACTION_GET_CONTENT);

        //Set type of media to open (i.e. images, instead of videos or both)
        choosePictureIntent.setType("image/*");

        //Get content URI of FileProvider for which picture file from camera is to be saved to
        Uri uriFileProvider = FileProvider.getUriForFile(
                getActivity(),
                "com.petertieu.android.mepix.fileprovider",
                mPictureFile
        );


        //Add content URI as extra to the intent
        choosePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFileProvider);

        //Create PackageManager (which has access to all aps installed in the device)
        PackageManager packageManager = getActivity().getPackageManager();

        //Query PackageManager to obtain list of activities/apps that match conditions of choosePictureIntent
        List<ResolveInfo> galleryActivities = packageManager.queryIntentActivities(
                choosePictureIntent, //(Intent): The intent to open camera
                PackageManager.MATCH_DEFAULT_ONLY //Filter the query to only intents of the DEFAULT category
        );

        //Sort through all activities resolved (in the list)
        for (ResolveInfo resolvedActivity : galleryActivities){
            //Grant permission for the resolved activity to write to the URI of the FileProvider
            getActivity().grantUriPermission(
                    resolvedActivity.activityInfo.packageName, //(String): The resolved activity
                    uriFileProvider, //(Uri): URI of FileProvider for which to grant access to
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION //(int): The access mode - allow writing to file
            );
        }



        //Start the activity, expecting results to be returned (via onActivityResult(..))
        startActivityForResult(Intent.createChooser(choosePictureIntent, "Select Picture"), REQUEST_CODE_PICTURE_GALLERY);
    }





    //Update picture view
    private void updatePictureView(){

        //If picture file does NOT exist
        if (mPictureFile == null || !mPictureFile.exists()){

            //Talkback accessibility: Associate textual description to 'empty' view
            mPictureView.setContentDescription(getString(R.string.pix_no_picture_description));

//            Toast.makeText(getActivity(), "No mPictureFile", Toast.LENGTH_SHORT).show();
        }

        //If picture file EXISTS
        else{

            //Get picture in bitmap 'scaled' format
            Bitmap pictureBitmap = PictureUtility.getScaledBitmap(mPictureFile.getPath(), getActivity());

            //Rotate picture bitmap to correct orientation - as pictureBitmap is 90 degrees off-rotation
            Matrix matrix = new Matrix(); //Create Matrix object for transforming co-ordinates
            matrix.postRotate(90); //Set rotation for Matrix
            Bitmap pictureBitmapCorrectOrientation = Bitmap.createBitmap(pictureBitmap , 0, 0, pictureBitmap .getWidth(), pictureBitmap.getHeight(), matrix, true); //Rotate picture Bitmap


            //Set picture ImageView view to bitmap version
            mPictureView.setImageBitmap(pictureBitmapCorrectOrientation);

            //Talkback accessbility: Associate textual description to 'existing' view
            mPictureView.setContentDescription(getString(R.string.pix_picture_description));

//            Toast.makeText(getActivity(), "mPictureFile exists", Toast.LENGTH_SHORT).show();

        }

    }





    //Override onCreateOptionsMenu(..) fragment lifecycle callback method
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){

        //Log lifecycle callback
        Log.i(TAG, "onCreateOptionsMenu(..) called");

        //Inflate a menu hierarchy from specified resource
        menuInflater.inflate(R.menu.fragment_pix_detail, menu);
    }





    //Override onOptionsItemSelected(..) fragment lifecycle callback method
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        //Log lifecycle callback
        Log.i(TAG, "onOptionsItemSelected(..) called");

        //Get
        switch (menuItem.getItemId()) {

            case (R.id.delete_pix):

                //Display 'delete confirmation' dialog
                deleteConfirmationDialog();
                return true;

            case(R.id.share_pix):
                OnClickShare(mViewToShare);
                return true;

            case(R.id.update_pix_location):

                if(isGpsEnabled()) {
                    Log.i(TAG, "CONNECTED to locations");
                    createLocationRequest();
                    updateLocationConfirmationDialog();
//                updatePixLocationMenuItemPressed = true;
//                createLocationRequest();
//                updatePixLocation();
                }

                else{
                    Log.i(TAG, "NOT connected to locations");
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setMessage("You need to turn on Location Services");
                    dialog.setPositiveButton(getContext().getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            getContext().startActivity(myIntent);
                            //get gps
                        }
                    });
                    dialog.setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub

                        }
                    });
                    dialog.show();
                }

                return true;


            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }





    private void deleteConfirmationDialog(){

        //Create FragmentManager
        FragmentManager fragmentManager = getFragmentManager();

        //Create DatePickerFragment fragment
        PixDeleteConfirmationDialogFragment pixDeleteConfirmationDialog = PixDeleteConfirmationDialogFragment.newInstance(mPix.getTitle(), mPix.getDescription());

        //Start the dialog fragment
        pixDeleteConfirmationDialog.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_DIALOG_FRAGMENT_DELETE_CONFIRMATION);

        //Show dialog
        pixDeleteConfirmationDialog.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_DELETE_CONFIRMATION);
    }



    LinearLayout mViewToShare;









    public void OnClickShare(View view) {

        //Eliminate "cursor" showing up prior to 'screenshot'
        mTitle.setCursorVisible(false);
        mTagEditText.setCursorVisible(false);
        mDescription.setCursorVisible(false);


        //Eliminate "text highlight" showing up prior to 'screenshot'
        Editable editableTitle = mTitle.getText();
        String titleString = editableTitle.toString();
        mTitle.setText(titleString);

        Editable editableTag = mTagEditText.getText();
        String tagString = editableTag.toString();
        mTagEditText.setText(tagString);

        Editable editableDescription = mDescription.getText();
        String descriptionString = editableDescription.toString();
        mDescription.setText(descriptionString);



        Bitmap bitmap = getBitmapFromView(mViewToShare);




        mTitle.setCursorVisible(true);
        mTagEditText.setCursorVisible(true);
        mDescription.setCursorVisible(true);




        try {
            File file = new File(getActivity().getExternalCacheDir(), "logicchip.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share Picture via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }




    public void updateLocationConfirmationDialog(){
        //Create FragmentManager
        FragmentManager fragmentManager = getFragmentManager();

        //Create DatePickerFragment fragment
        //Argument 1: The STORED address line (i.e. the address line that is stored in the SQLiteDatabase)
        //Argument 2: The CURRENT address line (i.e. the address line obtained from the location request, i.e. the address line of the CURRENT LOCATION)
        PixUpdateLocationConfirmationDialogFragment pixUpdateLocationConfirmation = PixUpdateLocationConfirmationDialogFragment.newInstance(mPix.getAddress(), mAddressOutput.getAddressLine(0));

        //Start the dialog fragment
        pixUpdateLocationConfirmation.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION);

        //Show dialog
        pixUpdateLocationConfirmation.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION);

        Log.i(TAG, "updateLocationConfirmationDialog() called");
    }







    LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updatePixLocation(){

        try{
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
        catch (SecurityException securityException){
            Log.e(TAG, "Error requesting location updates", securityException);
        }

    }



    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }




















    //Override onActivityCreated() fragment lifecycle callback method
    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);

        //Log in Logcat
        Log.i(TAG, "onActivityCreated() called");
    }






    //Override onStart() fragment lifecycle callback method
    @Override
    public void onStart(){
        super.onStart();

        //Log in Logcat
        Log.i(TAG, "onStart() called");




        //REPEATED AGAIN FOR onStart() so that when the user returns from enabling locations, the app returns to onStart() instead of onCreate()!!
        //========== Request for Location (dangerous) permissions (from Location permission group) ====================================================

        //Create AddressResultReceiver object, passing a Handler to it
        mAddressResultReceiver = new AddressResultReceiver(new Handler());

        //If location permissions requested in the Manifest have NOT been granted
        if (hasLocationPermission() == false) {

            //Request (user) for location permissions - as they are 'dangerous' permissions
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_CODE_FOR_LOCATION_PERMISSIONS);
        }

        //If location permissions requested in the Manifest HAVE been granted
        else {

            //========== Configure location services ====================================================
            //Create FusedLocationProviderClient object - to get location fix
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            //Try risky task - addSuccessListener could throw a SecurityException exception
            try {
                //Get most recent location available via getLastLocation(). NOTE: getLastLocation() runs ASYNCHRONOUSLY, whereas .
                mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {

                    //Listen for when location has been obtained via FusedLocationClient
                    @Override
                    public void onSuccess(Location location) {
                        //If location does not
                        if (location == null) {
                            Log.v(TAG, "Location does not exist");
                        }

                        //Stash mLocation instance reference variable to local reference variable of location object
                        mLocation = location;

                        //In some rare cases, location returned can be null
                        if (mLocation == null) {
                            return;
                        }

                        //If Geocoder is NOT present (Geocoder: object to convert location fix (lat/lon values) to linguistic address
                        if (!Geocoder.isPresent()) {
                            Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                            return;
                        }

                        //Start IntentService to perform reverse geocoding (i.e. obtaining address from location fix (i.e. lat/lon values))
                        startIntentService();
                    }
                });
            }
            //Catch any SecurityException thrown by addSuccessListener(..)
            catch (SecurityException securityException) {
                Log.e(TAG, "Error creating location service", securityException);
            }
        }





        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                //If updated location does not
                if (locationResult == null) {
                    Log.v(TAG, "Updated location does not exist");
                }

                mLocation = locationResult.getLastLocation();

                //In some rare cases, location returned can be null
                if (mLocation == null) {
                    return;
                }

                //If Geocoder is NOT present (Geocoder: object to convert location fix (lat/lon values) to linguistic address
                if (!Geocoder.isPresent()) {
                    Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                    return;
                }


                mAddressResultReceiver = new AddressResultReceiver(new Handler());

                //Start IntentService to perform reverse geocoding (i.e. obtaining address from location fix (i.e. lat/lon values))
                startIntentService();

            }
        };




    }





    //Override onResume() fragment lifecycle callback method
    @Override
    public void onResume(){
        super.onResume();

        //Log in Logcat
        Log.i(TAG, "onResume() called");
    }





    //Override onPause() fragment lifecycle callback method
    @Override
    public void onPause(){
        super.onPause();

        //Log in Logcat
        Log.i(TAG, "onPause() called");
    }





    //Override onStop() fragment lifecycle callback method
    @Override
    public void onStop(){
        super.onStop();

        //Log in Logcat
        Log.i(TAG, "onStop() called");

        stopLocationUpdates();
    }





    //Override onDestroyView() fragment lifecycle callback method
    @Override
    public void onDestroyView(){
        super.onDestroyView();

        //Log in Logcat
        Log.i(TAG, "onDestroyView() called");
    }





    //Override onDestroyView() fragment lifecycle callback method
    @Override
    public void onDestroy(){
        super.onDestroy();

        //Log in Logcat
        Log.i(TAG, "onDestroy() called");
    }





    //Override onDetach() fragment lifecycle callback method
    @Override
    public void onDetach(){
        super.onDetach();

        //Log in Logcat
        Log.i(TAG, "onDetach() called");

        //Null mCallbacks ref. var. (to PixDetailFragment.Callbacks object)
        mCallbacks = null;
    }





    //Override onActivityResult(..) callback method
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        //Log in Logcat
        Log.i(TAG, "onActivityResult(..) called");

        //If a result code DOES NOT exist
        if (resultCode != Activity.RESULT_OK) {
            return;
        }


        //If request code matches the date dialog fragment's
        if (requestCode == REQUEST_CODE_DIALOG_FRAGMENT_DATE) {

            //Get Date object from date dialog fragment
            Date setDate = (Date) intent.getSerializableExtra(DatePickerFragment.EXTRA_DATE);

            //Set new date for Pix
            mPix.setDate(setDate);

            //Set new date display for date button
            mDateButton.setText(mDateFormat.format("EEE d MMMM yyyy", mPix.getDate()));

            //Update Pix (upon new date change)
            updatePix();

        }


        if (requestCode == REQUEST_CODE_DIALOG_FRAGMENT_DELETE_CONFIRMATION){

            boolean confirmDelete = intent.getBooleanExtra(PixDeleteConfirmationDialogFragment.EXTRA_PIX_DELETE_CONFIRMATION, false);



            if (confirmDelete == true) {


                //Delete Pix from SQLiteDatabase, "pixes"
                PixManager.get(getActivity()).deletePix(mPix);

                //Call callback method to delete Pix
                mCallbacks.onPixDeleted(mPix);


                //Check if PixViewPagerActivity activity is running (i.e. we are in single-pane layout, i.e. sw < 600dp).
                // This check is important, since we want to close this activity IF it is running... NOT PixListActivity (which also hosts PixDetailFragment) when in two-pane layout)
                // We do not want to close the
                if (PixViewPagerActivityLifecycleTracker.isActivityVisible()) {
                    //Finish the PixViewPagerActivity activity, so that the detail view would pop off the stack, revealing the list view
                    getActivity().finish();
                }


                //Update the Pix SQLiteDatabase and two-pane UI (upon Pix delete)
                updatePix();


                //======= Hide soft keyboard ========
                //Get InputMethodManager object
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                //Request to hide soft keyboard. Argument 1 (IBinder): Any view visible on screen (e.g. mTitle)
                inputMethodManager.hideSoftInputFromWindow(mFavoritedButton.getWindowToken(), 0);


                //======= Display Toast on Pix delete ========
                //If no Pix title exists or is empty
                if (mPix.getTitle() == null || mPix.getTitle().isEmpty()) {
                    Toast.makeText(getContext(), Html.fromHtml("<i>" + "*Untitled*" + "</i>" + " Pix Deleted"), Toast.LENGTH_LONG).show();
                }
                //If Pix title exists or is not empty
                else {
                    Toast.makeText(getContext(), Html.fromHtml("Pix Deleted: " + "<b>" + mPix.getTitle() + "</b>"), Toast.LENGTH_LONG).show();
                }

                //Update Pix SQLiteDatabase and two-pane UI (upon changes to the Pix)
                updatePix();
            }

        }


        //If resultCode matches the contact's activity and its intent exists
        if (requestCode == REQUEST_CODE_CONTACT && intent != null) {

            //Get contact's URI from contact intent
            Uri contactUri = intent.getData();

            //Get display name
            String[] displayNameField = new String[]{ContactsContract.Contacts.DISPLAY_NAME};

            //Create cursor to query ContactsContract.Contacts table
            Cursor cursorDisplayName = getActivity().getContentResolver().query(
                    contactUri, //URI of the contact to query
                    displayNameField, //Contains display name of the contact to query
                    null,
                    null,
                    null
            );


            try {
                //Check if cursor contains results
                if (cursorDisplayName.getCount() == 0) {
                    return;
                }

                //Move cursor to first field
                cursorDisplayName.moveToFirst();

                //Extract data (i.e. DISPLAY_NAME) that cursor points to
                String displayName = cursorDisplayName.getString(0);

                //If total Pix tag String is NOT empty
                if (!mTotalTag.isEmpty()) {
                    //Append total Pix tag String to display name of contact
                    mTotalTag = mTotalTag + ",\n- " + displayName;
                }
                //If total Pix tag String is EMPTY initially (i.e. no tags made, i.e. EditText not filled)
                else {
                    //Let total Pix tag String equal display name of contact
                    mTotalTag = "- " + displayName;
                }

                //Set tag field of Pix to obtained data
                mPix.setTag(mTotalTag);

                //Display obtained data to mTagButton
//                mTagButton.setText(displayName);

                mTagEditText.setText(mPix.getTag());

                //Update the Pix SQLiteDatabase and two-pane UI (upon change with Pix's tag field)
                updatePix();
            } finally {
                //Close cursor
                cursorDisplayName.close();
            }
        }


        //If resultCode matches camera activity
        if (requestCode == REQUEST_CODE_PICTURE_CAMERA) {

            //Get content URI of FileProvider for which picture file taken from camera has been saved
            Uri uriFileProvider = FileProvider.getUriForFile(
                    getActivity(), //
                    "com.petertieu.android.mepix.fileprovider", //(String): The authority of the FileProvider - defined in <provider> element in Manifest
                    mPictureFile //(File): The picture file
            );

            //Revoke persmission from camera from writing to FileProvider
            getActivity().revokeUriPermission(uriFileProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            //Update picture view
            updatePictureView();

            //Update Pix SQLiteDatabase and two-pane UI (upon any changes)
            updatePix();
        }


        //If reqsultCode matches gallery activity
        if (requestCode == REQUEST_CODE_PICTURE_GALLERY) {

            //Get content URI of FileProvider for which picture file taken from camera has been saved
            Uri uriFileProvider = FileProvider.getUriForFile(
                    getActivity(), //
                    "com.petertieu.android.mepix.fileprovider", //(String): The authority of the FileProvider - defined in <provider> element in Manifest
                    mPictureFile //(File): The picture file
            );



            Bitmap bm=null;
            if (intent != null) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), intent.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            mPictureView.setImageBitmap(bm);


            //Revoke permission from camera from writing to FileProvider
            getActivity().revokeUriPermission(uriFileProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


//            //Update picture view
//            updatePictureView();

            //Update Pix SQLiteDatabase and two-pane UI (upon any changes)
            updatePix();
        }









        if (requestCode == REQUEST_CODE_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION){

            boolean confirmUpdatePix = intent.getBooleanExtra(PixUpdateLocationConfirmationDialogFragment.EXTRA_PIX_UPDATE_LOCATION_CONFIRMATION, false);




            if (confirmUpdatePix == true) {


                updatePixLocationMenuItemPressed = true;
                createLocationRequest();
                updatePixLocation();





                //======= Hide soft keyboard ========
                //Get InputMethodManager object
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                //Request to hide soft keyboard. Argument 1 (IBinder): Any view visible on screen (e.g. mTitle)
                inputMethodManager.hideSoftInputFromWindow(mFavoritedButton.getWindowToken(), 0);



                //======= Display Toast - wrap it in a conditional statement so that it only shows up once instead of as many times as there are Pix objects in the ViewPager ========
                if (mPix.getAddress() != null || !mPix.getAddress().isEmpty()) {
                    Toast.makeText(getContext(), Html.fromHtml("Pix Location Updated"), Toast.LENGTH_LONG).show();
                }


                //Update Pix SQLiteDatabase and two-pane UI (upon changes to the Pix)
//                updatePix();
            }

        }



    }




}
