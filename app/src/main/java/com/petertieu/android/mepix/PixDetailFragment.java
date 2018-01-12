package com.petertieu.android.mepix;

import android.app.Activity;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.Editable;
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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Peter Tieu on 14/12/2017.
 */



//Fragment for the DETAIL VIEW
public class PixDetailFragment extends SupportMapFragment {

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
    private static final String IDENTIFIER_DIALOG_FRAGMENT_DELETE = "DialogDelete"; //Identifier of dialog fragment

    //Declare constants for tag requests
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_DATE = 0;  //Request code for receiving results from dialog fragment
    private static final int REQUEST_CODE_CONTACT = 1; //Request code for results returned from contact activity/app
    private static final int REQUEST_CODE_PICTURE_CAMERA = 2; //Request code for results returned from camer activity/app
    private static final int REQUEST_CODE_PICTURE_GALLERY = 3;
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_DELETE = 10; //Request code for receiving results from dialog fragment

    //Declare Callbacks interface reference variable
    private Callbacks mCallbacks;



    //Declare FusedLocationProviderClient - the entry point for interactng with FusedLocationProvider (to get location fix)
    private FusedLocationProviderClient mFusedLocationClient;


    //List all the permissions that we need,
    // Manifest.permission.ACCESS_FINE_LOCATION
    //AND
    // Manifest.permission.ACCESS_COARSE_LOCATION
    private static final String[] LOCATION_PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION};

    //Define requestCode for the REQUEST of the LOCATION FIX.
    //NOTE: This constant will be used in requestPermission(..) and onRequestPermissionResult(..)
    private static final int REQUEST_CODE_FOR_LOCATION_PERMISSIONS = 0;

    //Declare LatLng objec to hold Latitute/Longitude data of Pix
    LatLng latLngOfPix;
    double latitudeOfPix;
    double longitudeOfPix;

    protected Location mLocation;
    private AddressResultReceiver mAddressResultReceiver;


    String address;
    String city;
    String state;
    String country;
    String postalCode;
    String knownName;






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
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Log in Logcat
        Log.i(TAG, "onCreate(..) called");

        //Get the 'value' of the argument-bundle
        UUID pixId = (UUID) getArguments().getSerializable(ARGUMENT_PIX_ID);

        //Create Pix object
        mPix = new Pix();

        //Assign Pix object to Pix object from PixManager singleton
        mPix = PixManager.get(getActivity()).getPix(pixId);

        //Assign reference variable, mPictureFile, to picture file in FoleProvider
        mPictureFile = PixManager.get(getActivity()).getPictureFile(mPix);


        mAddressResultReceiver = new AddressResultReceiver(new Handler());

        if (hasLocationPermission()) {
            //At this point.. the permission has been granted, so we are ready to request for the location.
            //Activate the LOCATION REQUEST (findImage()),

            //Create FusedLocationProviderClient object
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

            //Try risky task - addSuccessListener could throw a SecurityException exception
            try {

                mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location == null) {

                            Log.v(TAG, "Location does not exist");
                        }



                        mLocation = location;

                        // In some rare cases the location returned can be null
                        if (mLocation == null) {
                            return;
                        }

                        if (!Geocoder.isPresent()) {
                            Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                            return;
                        }

                        startIntentService();


//                            latLngOfPix = new LatLng(location.getLatitude(), location.getLongitude());
//
//
//
//                            Log.i(TAG, Double.toString(latLngOfPix.latitude) + " 1");
//                            Log.i(TAG, Double.toString(latLngOfPix.longitude) + " 2");
//
//                            latitudeOfPix = latLngOfPix.latitude;
//                            longitudeOfPix = latLngOfPix.longitude;
//
////                            latitudeOfPix = -37.777500;
////                            longitudeOfPix = 144.872701;
//
//
//
//
//                            Geocoder geocoder;
//                            List<Address> addresses;
//                            geocoder = new Geocoder(getActivity(), Locale.getDefault());
//
//                            //Try risky task - getFromLocation(...) can throw IOException
//                            try{
//
//
//                                addresses = geocoder.getFromLocation(latitudeOfPix, longitudeOfPix, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//
//                                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//
//                                city = addresses.get(0).getLocality();
//
//                                state = addresses.get(0).getAdminArea();
//
//                                country = addresses.get(0).getCountryName();
//
//                                postalCode = addresses.get(0).getPostalCode();
//
//                                knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
//
//
//
//                                Log.i(TAG, Double.toString(latitudeOfPix) + " 3");
//                                Log.i(TAG, Double.toString(longitudeOfPix) + " 4");
//                            }
//                            catch (IOException ioException){
//                                Log.e(TAG, "Error obtaining latitude and longitude of Pix");
//                            }




                    }
                });
            }

            catch (SecurityException securityException){
                Log.e(TAG, "Error creating location service", securityException);
            }


        }
        //At this point... the permission (requested in the Manifest) hasn't been granted..
        else{
            // Step 2 of 3 (Handling permissions at runtime):
            // REQUEST...
            // ...for the permission.
            //requestPermission(String[] permissions, int requestCode) is from Fragment.
            // It requests the permissions in the 'permissions' parameter.
            //NOTE: These permissions will ALSO need to be in the Manifest (if they haven't already been added).
            //NOTE: requestPermissions(..) will invoke OnRequestPermissionsResult(..)
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_CODE_FOR_LOCATION_PERMISSIONS);
        }




        //Declare an options menu for the fragment
        setHasOptionsMenu(true);

    }






    //CHECK whether the location permission requested in the Manifest has been granted or not
    // first permission in the permission group (i.e. ACCESS_FINE_LOCATION permission)
    private boolean hasLocationPermission(){

        //checkSelfPermission(Context context, String permission) is a static method from ContextCompat.
        // It checks whether the permission parameter, 'permission', is granted to the activity.
        // If permission granted, it returns: PackageManager.PERMISSION_GRANTED (int value: 0).
        // If permission NOT granted, it returns: PackageManager.PERMISSION_DENIED (int value: -1).
        //NOTE: The permissions; ACCES_FINE_LOCATION and ACCESS_COARSE_LOCATION
        // are in the same PERMISSION GROUP
        // This PERMISSION GROUP is called: LOCATION.
        //The special thing about PERMISSION GROUPS is:
        // If a permission that is in a PERMISSION GROUP is granted,
        // this means that ALL permissions in that PERMISSION GROUP are granted, too.
        //NOTE: Other PERMISSION GROUPS include:
        //  CALENDAR, CAMERA, CONTACTS, MICROPHONE, PHONE, SENSORS, SMS, STORAGE.
        //So in this situation, if the permission: Manifest.permission.ACCESS_FINE_LOCATION (i.e. LOCATION_PERMISSION[0])
        // is granted, then we could assume that Manifest.permission.ACCESS_COARSE_LOCATION is also granted!
        int result = ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);


        //Return a boolean
        //true: IF Manifest.permission.ACCESS_FINE_LOCATION permission has been granted
        //false: IF Manifest.permission.ACCESS_FINE_LOCATION permission NOT granted
        return result == PackageManager.PERMISSION_GRANTED;
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
//        Log.i(TAG, city);
//        Log.i(TAG, state);
//        Log.i(TAG, country);
//        Log.i(TAG, postalCode);
//        Log.i(TAG, knownName);
        mLocationButton = (Button) view.findViewById(R.id.detail_pix_location);

//        mLocationButton.setText(city + " " + state + " " + country + " " + postalCode + " " + knownName);
//        mLocationButton.setText(Double.toString(latitudeOfPix).toString() + ", " + Double.toString(longitudeOfPix).toString());
//        mLocationButton.setText(address);

//        mLocationButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                mLocationButton.setText(Double.toString(latitudeOfPix).toString() + ", " + Double.toString(longitudeOfPix).toString());
//
//                mLocationButton.setText(address);
//
//            }
//        });



        //================ SET UP mFavoritedButton ==================================================================
        //Assign favorited button instance variable to its associated resource ID
        mFavoritedButton = (CheckBox) view.findViewById(R.id.detail_pix_favorited);

        //Set sate of favorited button to its equivalent in Pix object
        mFavoritedButton.setChecked(mPix.isFavorited());

        //Set listener for CheckBox
        mFavoritedButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            //Override onCheckedChanged(..) from CompountButton.OnCheckedChangedListener
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

        //Set text of the title to descirption instance variable of the Pix
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

                //Set dialog prompt items
                final CharSequence[] dialogItems = {"Take Picture", "Choose from Gallery"};

                //Create AlertDialog.Builder object
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                //Set title of dialog
                alertDialogBuilder.setTitle("Add a Picture");

                //Set items of dialog, and create listeners for them
                alertDialogBuilder.setItems(dialogItems, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        //If "Take Picture" dialog item is pressed
                        if (dialogItems[item].equals("Take Picture")) {

                            //Log in Logcat
                            Log.i(TAG, "*Take Photo* pressed");

                            //Open camera activity/app (as new task)
                            cameraIntent();
                        }

                        //If "Choose from Gallery" dialog item is pressed
                        else if (dialogItems[item].equals("Choose from Gallery")) {

                            //Log in Logcat
                            Log.i(TAG, "*Choose from Library* pressed");

                            //Open gallery activity/app (as new task)
                            galleryIntent();
                        }

//                        //If "Cancel" dialog item is pressed
//                        else if (dialogItems[item].equals("Cancel")) {
//
//                            //Log in Logcat
//                            Log.i(TAG, "*Cancel* pressed");
//
//                            //Close dialog
//                            dialog.dismiss();
//                        }
                    }
                });

                //Set dialog 'Cancel' button
                alertDialogBuilder.setNegativeButton(android.R.string.cancel, null);

                //Show dialog
                alertDialogBuilder.show();
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
                    ImageViewFragment pictureViewDialog = ImageViewFragment.newInstance(mPictureFile);

                    //Set PixDetailFragment as target fragment for the dialog fragment
                    pictureViewDialog.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_PICTURE_CAMERA);

                    //Create FragmentManager (which has access to all fragments)
                    FragmentManager fragmentManager = getFragmentManager();

                    //Show the fragment
                    pictureViewDialog.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_PICTURE);

                }
            }
        });


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





    //Call IntenService to obtain address from lat/lon data
    protected void startIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);

        //Pass AddressResultReceiver as extra to handle results of the location fix
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mAddressResultReceiver);

        ////Pass Location as extra, which contains lat/lon data of location fix to convert to address
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLocation);

        //Start IntentService to perform reverse geocoding
        getActivity().startService(intent);
    }





    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);


            if (resultCode == FetchAddressIntentService.Constants.FAILURE_RESULT){
                Toast.makeText(getActivity(), "Address not found", Toast.LENGTH_SHORT).show();
            }

            // Show a toast message if an address was found.
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                Toast.makeText(getActivity(), "Address found: " + mAddressOutput, Toast.LENGTH_SHORT).show();

                mLocationButton.setText(mAddressOutput);

//                if (locationSavedToDB){
//                    mLocationButton.setText(mPix.getLocation());
//                    locationSavedToDB = true;
//                }
//                else{
//                    mPix.setLocation(mAddressOutput);
//                }



            }

        }
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
                    resolvedActivity.activityInfo.packageName, //(String): The resoleved activity
                    uriFileProvider, //(Uri): URI of FileProvider for which to grant access to
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION //(int): The access mode - allow writing to file
            );
        }

        //Start the activity, expecting results to be returned (via onActivityResult(..))
        startActivityForResult(takePictureIntent, REQUEST_CODE_PICTURE_CAMERA);
    }





    //Open gallery activity/app
    private void galleryIntent(){

        //Create implicit intent (to open gallery actvity/app)
        Intent choosePictureIntent = new Intent();

        //Set action to open gallery activity/app
        choosePictureIntent.setAction(Intent.ACTION_GET_CONTENT);

        //Set type of media to open (i.e. images, instead of vides or both)
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
            //Grant permsision for the resolved activity to write to the URI of the FileProvider
            getActivity().grantUriPermission(
                    resolvedActivity.activityInfo.packageName, //(String): The resoleved activity
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

        //Log lifcycle callback
        Log.i(TAG, "onCreateOptionsMenu(..) called");

        //Inflate a menu hiearchy from specified resource
        menuInflater.inflate(R.menu.fragment_pix_detail, menu);
    }





    //Override onOptionsItemSelected(..) fragment lifecycle callback method
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        //Log lifecycle callback
        Log.i(TAG, "onOptionsItemSelected(..) called");

        //Get
        switch(menuItem.getItemId()){

            case(R.id.delete_pix):

//                //Display 'delete confirmation' dialog
//                deleteConfirmationDialog();

            //Delete Pix from SQLiteDatabase, "pixes"
            PixManager.get(getActivity()).deletePix(mPix);

            //Call callback method to delete Pix
            mCallbacks.onPixDeleted(mPix);

            //Update the Pix SQLiteDatabase and two-pane UI (upon Pix delete)
            updatePix();


            //======= Hide soft keyboard ========
            //Get InputMethodManager object
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            //Request to hide softw keyboard. Argument 1 (IBinder): Any view visible on screen (e.g. mTitle)
            inputMethodManager.hideSoftInputFromWindow(mFavoritedButton.getWindowToken(), 0);


            //======= Display Toast on Pix delete ========
            //If no Pix title exists or is empty
            if (mPix.getTitle() == null || mPix.getTitle().isEmpty()){
                Toast.makeText(getContext(), "Untitled  Pix deleted", Toast.LENGTH_LONG).show();
            }
            //If Pix title exists or is not empty
            else{
                Toast.makeText(getContext(), "Pix deleted:  " + mPix.getTitle(), Toast.LENGTH_LONG).show();
            }

            //Update Pix SQLiteDatabase and two-pane UI (upon changes to the Pix)
            updatePix();

            return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }





//    private void deleteConfirmationDialog(){
//
//        //Create FragmentManager
//        FragmentManager fragmentManager = getFragmentManager();
//
//        //Create DatePickerFragment fragment
//        PixDeleteFragment pixDeleteDialog = PixDeleteFragment.newInstance(mPix.getTitle());
//
//        //Start the dialog fragment
//        pixDeleteDialog.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_DIALOG_FRAGMENT_DELETE);
//
//        //Show dialog
//        pixDeleteDialog.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_DELETE);
//    }





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





    //Override onDettach() fragment lifecycle callback method
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
            Date newSetdate = (Date) intent.getSerializableExtra(DatePickerFragment.EXTRA_DATE);

            //Set new date for Pix
            mPix.setDate(newSetdate);

            //Set new date display for date button
            mDateButton.setText(mDateFormat.format("EEE d MMMM yyyy", mPix.getDate()));

            //Update Pix (upon new date change)
            updatePix();

        }


//        if (requestCode == REQUEST_CODE_DIALOG_FRAGMENT_DELETE){
//
//
//            //Delete Pix from SQLiteDatabase, "pixes"
//            PixManager.get(getActivity()).deletePix(mPix);
//
//            //Call callback method to delete Pix
//            mCallbacks.onPixDeleted(mPix);
//
//            //Update the Pix SQLiteDatabase and two-pane UI (upon Pix delete)
//            updatePix();
//
//
//            //======= Hide soft keyboard ========
//            //Get InputMethodManager object
//            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//
//            //Request to hide softw keyboard. Argument 1 (IBinder): Any view visible on screen (e.g. mTitle)
//            inputMethodManager.hideSoftInputFromWindow(mFavoritedButton.getWindowToken(), 0);
//
//
//            //======= Display Toast on Pix delete ========
//            //If no Pix title exists or is empty
//            if (mPix.getTitle() == null || mPix.getTitle().isEmpty()){
//                Toast.makeText(getContext(), "Untitled  Pix deleted", Toast.LENGTH_LONG).show();
//            }
//            //If Pix title exists or is not empty
//            else{
//                Toast.makeText(getContext(), "Pix deleted:  " + mPix.getTitle(), Toast.LENGTH_LONG).show();
//            }
//
//            updatePix();
//
//        }


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
                //Close curosr
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


            //Revoke persmission from camera from writing to FileProvider
            getActivity().revokeUriPermission(uriFileProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


//            //Update picture view
//            updatePictureView();

            //Update Pix SQLiteDatabase and two-pane UI (upon any changes)
            updatePix();
        }

    }




}
