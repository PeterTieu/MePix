package com.petertieu.android.mepix;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.text.format.DateFormat;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;




//Fragment for the DETAIL VIEW
public class PixDetailFragment extends Fragment{


    //==================================== Declare INSTANCE VARIABLES ============================================================================
    //UI instance variables
    private Pix mPix;                       //Pix - the single Pix object that the detail view revolves around
    private EditText mTitle;                //Title
    private CheckBox mFavoritedButton;      //Favorited Button
    private Button mDateButton;             //Date Button
    private Button mTagButton;              //Tag Button
    private EditText mTagEditText;          //Tag EditText
    private String mTotalTag;               //Tag String - to be displayed in Tag EditText
    private Button mLocationButton;         //Location Button
    private EditText mDescription;          //Description Button
    private ImageButton mPictureAddButton;  //Photo Button
    private ImageView mPictureView;         //Picture ImageView to DISPLAY the Pix Picture


    //OPERATIONS instance variables
    private Callbacks mCallbacks;   //Callback interface
    private File mPictureFile;      //File to CONTAIN the Pix Picture
    private DateFormat mDateFormat; //DateFormat object for formatting display of the Date
    LinearLayout mViewToShare;      //View of the Pix that is captured - for sharing


    //MAPS instance variables
    private FusedLocationProviderClient mFusedLocationClient;   //Entry point for interacting with FusedLocationProviderApi (to get location fix (i.e. lat/lon))
    protected Location mLocation;                               //Declare Location object to contain location fix (i.e lat/lon values)
    private Address mAddressOutput;                             //Address of Pix (returned from IntentService (FetchAddressIntentService), and picked up by ResultReceiver (AddressResultReceiver)
    private AddressResultReceiver mAddressResultReceiver;       //ResultReceiver to receive reverse geocoding results from the IntentService (FetchAddressIntentService)
    private boolean updatePixLocationMenuItemPressed = false;   //Flag to be triggered when the Locations Update menu item is pressed. This is used in an if-statement to reset the mLocationButton's display display (of the location)
    LocationRequest mLocationRequest;                           //Sets for location update properties (e.g. defines the interval, priority, etc. to request for location update) from FusedLocationProviderApi
    private LocationCallback mLocationCallback;                 //LocationCallback for getting a location update



    //==================================== Declare STRINGS ============================================================================
    //Define TAG for Logcat
    private static final String TAG = "PixDetailFragment";

    //ARGUMENT-BUNDLE KEY
    private static final String ARGUMENT_PIX_ID = "pix_id";

    //DIALOG FRAGMENT IDENTIFIERS
    private static final String IDENTIFIER_DIALOG_FRAGMENT_DATE = "DialogDate";                                                 //Identifier of dialog fragment of DatePicker
    private static final String IDENTIFIER_DIALOG_FRAGMENT_PICTURE = "IdentifierDialogFragmentPicture";                         //Identifier of dialog fragment of Picture ImageView
    private static final String IDENTIFIER_DIALOG_FRAGMENT_DELETE_CONFIRMATION = "DialogDeleteConfirmation";                    //Identifier of dialog fragment of Pix Delete
    private static final String IDENTIFIER_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION ="DialogUpdateLocationConfirmation";    //Identifier of dialog fragment of Location Confirmation

    //PERMISSION REQUEST STRINGS
    private static final String[] LOCATION_PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};    //Request for LOCATION permissions
    private static final String[] STORAGE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};                    //Request for STORAGE permissions

    //REQUEST CODES for PERMISSIONS
    private static final int REQUEST_CODE_FOR_LOCATION_PERMISSIONS = 1;                         //Request code for receiving results from system for Location Permissions
    private static final int REQUEST_CODE_FOR_STORAGE_PERMISSIONS = 2;                          //Request code to WRITE to external storage (to save a Picture to Gallery)

    //REQUEST CODES for RESULTS
    private static final int REQUEST_CODE_CONTACT = 3;                                          //Request code for receiving results from contact activity/app
    private static final int REQUEST_CODE_PICTURE_CAMERA = 4;                                   //Request code for receiving results from camera activity/app
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_DATE = 5;                             //Request code for receiving results from dialog fragment to select Pix date
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_DELETE_CONFIRMATION = 6;              //Request code for receiving results from dialog fragment to delete Pix
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION = 7;     //Request code for receiving results from dialog fragment to confirm Pix location update
    private static final int REQUEST_CODE_PICTURE_SAVED_TO_GALLERY = 8;                         //Request code for receiving results form dialog fragment to Pix picture being saved to gallery
    //private static final int REQUEST_CODE_PICTURE_GALLERY = 8;                                //Request code for receiving results from phone's gallery





    //==================================== Define CALLBACK INTERFACE ============================================================================
    //Declare callback interface
    interface Callbacks{

        //Callback method for when a Pix's instance variable is changed (for two-pane layout)
        void onPixUpdatedFromDetailView(Pix pix);

        //Callback method for when a Pix is deleted (for two-pane layout)
        void onPixDeleted(Pix pix);
    }




    //==================================== Define METHODS ============================================================================

    //Build 'encapsulating' constructor
    public static PixDetailFragment newInstance(UUID pixId){

        //Create Bunble object (i.e. argument-bundle)
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





    //Override onAttach() fragment lifecycle callback method
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        //Log in Logcat
        Log.i(TAG, "onAttach() called");

        //Define the callback interface reference variable
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

        //Assign reference variable, mPictureFile, to picture file in FileProvider
        mPictureFile = PixManager.get(getActivity()).getPictureFile(mPix);


        //If the user has JUST previously entered two-pane mode (i.e. sw > 600dp) AND two-pane mode now no longer exists,
        // since the user has rotated the screen, and is now back to one-pane mode (i.e. is now in the list view)...
        if (PixListActivity.hasEnteredTwoPaneMode == true && getActivity().findViewById(R.id.detail_fragment_container) == null) {
            //Disable the options menu of this detail view, which effectively removes the 'Delete' button from the toolbar while in list view
            setHasOptionsMenu(false);
        }
        //..all other times
        else{
            //Declare an options menu for the fragment
            setHasOptionsMenu(true);
        }



        //_______ Request for (dangerous) PERMISSION: WRITE_TO_EXTERNAL (from permission group: Storage) _________

        //If the STORAGE permissions have not been granted
        if (hasWriteExternalStoragePermission() == false){
            //Request for STORAGE permissions
            requestPermissions(STORAGE_PERMISSIONS, REQUEST_CODE_FOR_STORAGE_PERMISSIONS);
        }



        //_________ Request for (dangerous) PERMISSIONS from permission group: Location _________________

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

            //Try risky task - onAddSuccessListener could throw a SecurityException exception
            try {
                //Get most recent location available via getLastLocation(). NOTE: getLastLocation() runs ASYNCHRONOUSLY, whereas .
                mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {

                    //Listen for when location has been obtained via FusedLocationClient
                    @Override
                    public void onSuccess(Location location) {

                        //If location does not
                        if (location == null) {
                            //Log to Logcat
                            Log.v(TAG, "Location does not exist");
                        }

                        //Stash mLocation instance reference variable to local reference variable of location object
                        mLocation = location;

                        //In some RARE cases, location returned can be null
                        if (mLocation == null) {
                            //Exit from try-block
                            return;
                        }

                        //If Geocoder is NOT present (Geocoder: object to convert location fix (lat/lon values) to linguistic address - known as 'reverse geocoding')
                        if (!Geocoder.isPresent()){
                            //Toast to display that the Geocoder is NOT available
                            Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();

                            //Exit from try-block
                            return;
                        }

                        //Create ResultReceiver object, passing a Handler to it.
                        //NOTE: A ResultReceiver's purpose is to receive results from IntentService!
                        mAddressResultReceiver = new AddressResultReceiver(new Handler());

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



        //Declare LocationCallback method for updating the location
        //NOTE: Updating the location MUST be done AFTER a location has already been obtained (i.e. after reverse geocoding, as per above)
        mLocationCallback = new LocationCallback() {

            //Set listener for the callback
            @Override
            public void onLocationResult(LocationResult locationResult) {

                //If updated location does not
                if (locationResult == null) {
                    Log.v(TAG, "Updated location does not exist");
                }

                //Set mLocation instance variable to new Location object (i.e. the updated location)
                mLocation = locationResult.getLastLocation();

                //In some RARE cases, location returned can be null
                if (mLocation == null) {
                    return;
                }

                //If Geocoder is NOT present (Geocoder: object to convert location fix (lat/lon values) to linguistic address
                if (!Geocoder.isPresent()) {
                    Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                    return;
                }

                //Set mAddressResultReceiver instance variable to new AddressResultReceiver object
                mAddressResultReceiver = new AddressResultReceiver(new Handler());

                //Start IntentService to perform reverse geocoding (i.e. obtaining address from location fix (i.e. lat/lon values))
                startIntentService();
            }
        };

    }





    //======================== Define HELPER METHODS used in onCreate(..) =============================================

    //Check if (dangerous) permissions from the STORAGE permission group have been granted (by the user)
    private boolean hasWriteExternalStoragePermission(){

        //Check if the permissions have been granted
        //IF granted.. result = PakageManager.PERMISSION_GRANTED, else PackageManager.PERMISSON_DENIED
        int result = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //Return the result
        return (result == PackageManager.PERMISSION_GRANTED);
    }


    //Check if (dangerous) permissions from the STORAGE permission group have been granted (by the user)
    private boolean hasLocationPermission(){

        //If permission is granted, result = PackageManager.PERMISSION_GRANTED (else, PackageManager.PERMISSION_DENIED).
        //NOTE: Permissions ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION are in the same PERMISSION GROUP, called ADDRESS.
            //If one permission is a permission group is granted/denied access, the same applies to all other permissions in that group.
            // Other groups include: CALENDAR, CAMERA, CONTACTS, MICROPHONE, PHONE, SENSORS, SMS, STORAGE.
        int result = ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);

        //Return a boolean for state of location permission
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

                    //Update SQLiteDatabase of Pix to account for data added/updated for mLocation field (i.e. address)
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




    int newLinesCount = 1;
    int tempNum = newLinesCount;
    List<String> differentLinesArrayList;
    int count = 0;
    boolean hasAsteriskOnPreviousLine = false;
    String text;





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

                //Update SQLiteDatabase of Pix to account for data added/updated for mLocation field (i.e. address)
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

        //If address stored in Pix EXISTS.
        //Without this if-block: When the location is disabled, mLocationButton would not display the current/stored location.
        // It would just display the default text of the mLocationButton, i.e. "Unable to fetch location"
        if (mPix.getAddress() != null) {
            //Display the stored address in mLoationButton
            mLocationButton.setText(mPix.getAddress());
        }

        //Set listener for location button
        mLocationButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //If location is enabled (by user)
                if(isGpsEnabled()) {
                    //Create Intent to open MapsActivity
                    Intent mapsActivityIntent = MapsActivity.newIntent(getActivity(), mPix.getLatitude(), mPix.getLongitude(), mPix.getAddress());

                    //Start intent to open MapsActivity
                    getActivity().startActivity(mapsActivityIntent);
                }
                //If location is NOT enabled (by user)
                else {
                    //Open "Enable Location" dialog - for the user to enable location via Settings
                    enableLocationDialog();
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
            public void beforeTextChanged(CharSequence charSequence, int i1, int i2, int i3) {
                //Do nothing

//                newLinesCount = mDescription.getLineCount();
//                Log.d("LINECOUNT", Integer.toString(newLinesCount));
            }



            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                //Set description of the Pix to the current String in the EditText
                mPix.setDescription(charSequence.toString());



//                mDescription.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//                    @Override
//                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//
//                        if (actionId == EditorInfo.IME_ACTION_DONE){
//                            newLinesCount++;
//                            return true;
//                        }
//
//                        if (  (keyEvent.getAction() == KeyEvent.KEYCODE_E) || (keyEvent.getAction() == KeyEvent.ACTION_DOWN) || ((keyEvent.getAction() == KeyEvent.ACTION_UP) ) ){
//                            newLinesCount++;
//                        }
//
//                        return true;
//                    }
//                });
//
//
//                Log.d("NEWLINE", Integer.toString(newLinesCount));










//                StringReader sr = new StringReader(mDescription.getText().toString());
//                LineNumberReader lnr = new LineNumberReader(sr);
//                try {
//                    while (lnr.readLine() != null){}
//                    newLinesCount = lnr.getLineNumber();
//                    Log.d(TAG, "NEWLINE = " + newLinesCount);
//                    lnr.close();
//                } catch (IOException e) {
//                    newLinesCount = mDescription.getLineCount();
//
//                } finally {
//                    sr.close();
//                }
//
//                Log.d("NEWLINECOUNT", Integer.toString(newLinesCount));












                if (charSequence.toString().indexOf("\n") != -1){
                    newLinesCount = mDescription.getLineCount();
                    Log.d("LINECOUNT", Integer.toString(newLinesCount));
                }

//                Log.d("LINECOUNT", Integer.toString(newLinesCount));








                //Total text
                String descriptionText = mDescription.getText().toString();

                String[] differentLines = {};







                try {
                    if (!descriptionText.isEmpty() || descriptionText != null) {
                        //String array of all the lines separted by newline
                        differentLines = descriptionText.split("\n");
                    }


                    if (differentLines != null) {
                        //Get individual lines
                        for (String rawLine : differentLines) {

                            Log.d("TESTLINE", rawLine);


                            if (rawLine.charAt(0) == '*') {

                                Log.d("HASAST", rawLine);
                            }
                        }
                    }
                } catch (RuntimeException runtimeException){

                }



                differentLinesArrayList = new ArrayList<String > (Arrays.asList(descriptionText.split("\n")));


                for(String rawString : differentLinesArrayList){

                    Log.d("ARRAY", rawString);

                    if (rawString.startsWith("*")){
                        Log.d("ARRAYAST", rawString);
                    }

                }



                text = charSequence.toString() + "*";




                for (count = 0; count < differentLinesArrayList.size(); count++) {

                    //If a newline has been added (i.e. '\n' is detected, i.e. ENTER button is pressed)
                    if (tempNum != newLinesCount ) {



                        if (tempNum != 1) {

                            Log.d("NEWLINETEST", "NEWLINE DETECTED");


                            mDescription.removeTextChangedListener(this);
                            mDescription.setText(text);
                            mDescription.setSelection(text.length());
                            mDescription.addTextChangedListener(this);

                        }
                        tempNum = newLinesCount;
                    }













//                    text = charSequence.toString() + "*";
//                    mDescription.removeTextChangedListener(this);
//                    mDescription.setText(text);
//                    mDescription.setSelection(text.length());
//                    mDescription.addTextChangedListener(this);









                    //If the previous line has got asterisk
                    if (differentLinesArrayList.get(count).startsWith("*")) {
                        Log.d("ASTTEST", differentLinesArrayList.get(count));
                    }






                    //If the previous line has got asterisk
                    if (differentLinesArrayList.get(count).startsWith("*") && (charSequence.toString().indexOf("\n") != -1)) {
                        Log.d("ASTTEST", differentLinesArrayList.get(count));

                    }



                }



                //Update the Pix
                updatePix();

            }










            @Override
            public void afterTextChanged(Editable editable) {
                //Do nothing





            }
        });



//        mDescription.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction()==KeyEvent.ACTION_DOWN){
//                    Toast.makeText(getActivity(), mPix.getDescription(), Toast.LENGTH_SHORT).show();
//                }
//
//                return true;
//            }
//        });


//        mDescription.setOnKeyListener(new View.OnKeyListener() {
//
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event)
//            {
//                if (keyCode == event.KEYCODE_ENTER)
//                {
//                    Log.i(TAG, "captured");
//
//                    return false;
//                }
//
//                return false;
//            }
//        });









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

                //Open device's camera activity/app
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

                    //Open picture view DialogFragment
                    ImageViewDialogFragment pictureViewDialog = ImageViewDialogFragment.newInstance(mPictureFile, mPix.getTitle(), mPix.getDate());

                    //Set PixDetailFragment as target fragment for the dialog fragment
                    pictureViewDialog.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_PICTURE_SAVED_TO_GALLERY);

                    //Create FragmentManager (which has access to all fragments)
                    FragmentManager fragmentManager = getFragmentManager();

                    //Show the fragment
                    pictureViewDialog.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_PICTURE);
                }
            }
        });




        //================ SET UP mViewToShare ==================================================================
        //Define the View to capture in order to share the Pix.
        // In this case, it is the LinearLayout View
        mViewToShare = (LinearLayout) view.findViewById(R.id.pix_detail_linear_layout);


        //Return the View
        return view;
    }





    //======================== Define HELPER METHODS used in onCreateView(..) =============================================

    //Update Pix SQLiteDatabase and two-pane UI (upon any changes)
    private void updatePix(){

        //Update the SQLite database based on the Pix passed
        PixManager.get(getActivity()).updatePixOnDatabase(mPix);

        //Update PixListFragment() in 'real-time' for two-pane layout
        mCallbacks.onPixUpdatedFromDetailView(mPix);
    }





    //Open "Enable Location" dialog - for the user to enable location via Settings
    //NOTE: This is done if location is not enabled
    public void enableLocationDialog(){

            //Log in Logcat
            Log.i(TAG, "Location not enabled");

            //Create AlertDialog object
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());

            //Set-up custom title to display in the dialog
            TextView dialogTitle = new TextView(getActivity()); //Create TextView object
            dialogTitle.setText(R.string.prompt_enable_location); //Set text on TextView
            dialogTitle.setTextColor(getResources().getColor(R.color.colorButton)); //Set color of text
            dialogTitle.setTextSize(22); //Set size of text
            dialogTitle.setGravity(Gravity.CENTER); //Set position of text in the title box of the dialog
            dialogTitle.setTypeface(null, Typeface.BOLD); //Set the text to be bold
            dialogTitle.setBackgroundColor(getResources().getColor(R.color.yellow)); //Set background color title box of the dialog

            dialog.setCustomTitle(dialogTitle); //Set the dialog's title block to be the TextView defined (above)
            dialog.setMessage(Html.fromHtml("Location connectivity is off" + "<br>" + "</br>"+ "Turn on Location in Settings")); //Set message
            dialog.setPositiveButton(getContext().getResources().getString(R.string.settings), new DialogInterface.OnClickListener() { //Set Positive button for dialog
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    //Create Intent to open Settings to disable location
                    Intent loationSourceSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    //Start the Intent
                    getContext().startActivity(loationSourceSettingsIntent);
                }
            });
            dialog.setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() { //Set Negative button for dialog
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    //Do nothing
                }
            });
            dialog.show(); //Show dialog

    }





    //Check if the GPS on the phone is enabled
    public boolean isGpsEnabled(){

        //Create a LocationManager object
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        //Check if the GPS AND network provider are enabled
        return ( locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) );
    }





    //Open device's camera activity/app
    private void cameraIntent(){

        //Create implicit intent with action to open camera activity/app
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //Create PackageManager (which has access to all aps installed in the device)
        PackageManager packageManager = getActivity().getPackageManager();

        //Set boolean for condition: Picture file exists AND implicit intent can resolve camera activity via PackageManager
        boolean canTakePicture = (mPictureFile != null) && (takePictureIntent.resolveActivity(packageManager) != null);

        //Enable the mPictureAddButton, based on whether a suitable camera app is resolved, and the availability of the File to save the picture (in the FileProvider)
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
                PackageManager.MATCH_DEFAULT_ONLY //(int): Filter the query to only intents of the DEFAULT category
        );

        //Sort through all activities resolved (in the list)
        for (ResolveInfo resolvedActivity : cameraActivities){
            //Grant permsision for the resolved camera activity to write to the URI of the FileProvider
            getActivity().grantUriPermission(
                    resolvedActivity.activityInfo.packageName, //(String): The resolved activity
                    uriFileProvider, //(Uri): URI of FileProvider for which to grant access to
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION //(int): The access mode - allow writing to file
            );
        }

        //Start the activity, expecting results to be returned (via onActivityResult(..))
        startActivityForResult(takePictureIntent, REQUEST_CODE_PICTURE_CAMERA);
    }





//    //Open gallery activity/app
//    private void galleryIntent(){
//
//        //Create implicit intent (to open gallery activity/app)
//        Intent choosePictureIntent = new Intent();
//
//        //Set action to open gallery activity/app
//        choosePictureIntent.setAction(Intent.ACTION_GET_CONTENT);
//
//        //Set type of media to open (i.e. images, instead of videos or both)
//        choosePictureIntent.setType("image/*");
//
//        //Get content URI of FileProvider for which picture file from camera is to be saved to
//        Uri uriFileProvider = FileProvider.getUriForFile(
//                getActivity(),
//                "com.petertieu.android.mepix.fileprovider",
//                mPictureFile
//        );
//
//
//        //Add content URI as extra to the intent
//        choosePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFileProvider);
//
//        //Create PackageManager (which has access to all aps installed in the device)
//        PackageManager packageManager = getActivity().getPackageManager();
//
//        //Query PackageManager to obtain list of activities/apps that match conditions of choosePictureIntent
//        List<ResolveInfo> galleryActivities = packageManager.queryIntentActivities(
//                choosePictureIntent, //(Intent): The intent to open camera
//                PackageManager.MATCH_DEFAULT_ONLY //Filter the query to only intents of the DEFAULT category
//        );
//
//        //Sort through all activities resolved (in the list)
//        for (ResolveInfo resolvedActivity : galleryActivities){
//            //Grant permission for the resolved activity to write to the URI of the FileProvider
//            getActivity().grantUriPermission(
//                    resolvedActivity.activityInfo.packageName, //(String): The resolved activity
//                    uriFileProvider, //(Uri): URI of FileProvider for which to grant access to
//                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION //(int): The access mode - allow writing to file
//            );
//        }
//
//
//
//        //Start the activity, expecting results to be returned (via onActivityResult(..))
//        startActivityForResult(Intent.createChooser(choosePictureIntent, "Select Picture"), REQUEST_CODE_PICTURE_GALLERY);
//    }





    //Update picture view
    private void updatePictureView(){

        //If picture file does NOT exist
        if (mPictureFile == null || !mPictureFile.exists()){
            //Talkback accessibility: Associate textual description to 'empty' view
            mPictureView.setContentDescription(getString(R.string.pix_no_picture_description));
        }
        //If picture file EXISTS
        else{
            //Get picture in bitmap 'scaled' format
            Bitmap pictureBitmap = PictureUtility.getScaledBitmap(mPictureFile.getPath(), getActivity());

            //Rotate picture bitmap to correct orientation - as pictureBitmap is 90 degrees off-rotation
            Matrix matrix = new Matrix(); //Create Matrix object for transforming co-ordinates
            matrix.postRotate(90); //Set rotation for Matrix
            Bitmap pictureBitmapCorrectOrientation = Bitmap.createBitmap(pictureBitmap , 0, 0, pictureBitmap .getWidth(), pictureBitmap.getHeight(), matrix, true); //Rotate picture Bitmap

            //Set picture ImageView view to the Bitmap
            mPictureView.setImageBitmap(pictureBitmapCorrectOrientation);

            //Talkback accessbility: Associate textual description to 'existing' view
            mPictureView.setContentDescription(getString(R.string.pix_picture_description));

//            mPictureView.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_gallery));
//            mPictureFile.delete();
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

        //Scan through the MenuItem IDs
        switch (menuItem.getItemId()) {


            //"Delete Pix" MenuItem
            case (R.id.delete_pix):
                //Open 'Delete Confirmation' dialog
                deleteConfirmationDialog();
                return true;


            //"Share Pix" MenuItem
            case(R.id.share_pix):
                //Share the Pix
                sharePix(mViewToShare);
                return true;


            //"Update Pix" MenuItem
            case(R.id.update_pix_location):
                //If location is enabled (by user)
                if(isGpsEnabled()) {
                    //Log to Logcat
                    Log.i(TAG, "CONNECTED to locations");

                    //Open 'Update Location Confirmation' dialog
                    updateLocationConfirmationDialog();
                }
                //If location is NOT enabled (by user)
                else{
                    //Open "Enable Location" dialog - for the user to enable location via Settings
                    enableLocationDialog();
                }
                return true;


            //Set default values
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }





    //======================== Define HELPER METHODS used in onOptionsItemSelected(..) =============================================

    //Open 'Delete Confirmation' dialog
    private void deleteConfirmationDialog(){

        //Create FragmentManager
        FragmentManager fragmentManager = getFragmentManager();

        //Create DatePickerFragment fragment
        PixDeleteConfirmationDialogFragment pixDeleteConfirmationDialog = PixDeleteConfirmationDialogFragment.newInstance(mPix.getTitle(), mPix.getDescription());

        //Start the dialog fragment, setting PixDetailFragment as the target fragment of this dialog
        pixDeleteConfirmationDialog.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_DIALOG_FRAGMENT_DELETE_CONFIRMATION);

        //Show dialog
        pixDeleteConfirmationDialog.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_DELETE_CONFIRMATION);
    }





    //Share the Pix
    public void sharePix(View view) {

        //Unable the EditText views from showing any cursors - prior to the 'screenshot'
        mTitle.setCursorVisible(false);
        mTagEditText.setCursorVisible(false);
        mDescription.setCursorVisible(false);


        //Unable the EditText view from showing 'highlighted text' - prior to the 'screenshot'
        Editable editableTitle = mTitle.getText();
        String titleString = editableTitle.toString();
        mTitle.setText(titleString);

        Editable editableTag = mTagEditText.getText();
        String tagString = editableTag.toString();
        mTagEditText.setText(tagString);

        Editable editableDescription = mDescription.getText();
        String descriptionString = editableDescription.toString();
        mDescription.setText(descriptionString);


        //'Screenshot' the View to share, and store it in a Bitmap object
        Bitmap viewToShareBitmap = getBitmapFromView(mViewToShare);


        //Re-enable the EditText views to show cursors
        mTitle.setCursorVisible(true);
        mTagEditText.setCursorVisible(true);
        mDescription.setCursorVisible(true);


        //Try 'risky' task - getExternalCatcheDir() can throw an Exception
        try {
            //Get absolute path to application-specific directory on the primary shared/external storage device to store cache files
            //NOTE: These files are internal to the application, and not typically visible to the user as media
            File file = new File(getActivity().getExternalCacheDir(), "pixViewToShare.png");

            //Create the FileOutputStream to write data to the File
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            //Compress the bitmap into the FileOutputStream
            viewToShareBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

            //Force the buffered output bytes to be written out
            fileOutputStream.flush();

            //Closes the output stream and releases any system resources associated with the stream
            fileOutputStream.close();

            //Set the owner's read permission for the abstract pathname
            file.setReadable(true, false);

            //Set Intent to send file
            final Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);

            //Set the special flag controlling how this intent is handled
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //Add the file to URI of the file as extra in the intent
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            //Set image type as png
            sendIntent.setType("image/png");

            //Start chooser activity
            startActivity(Intent.createChooser(sendIntent, "Share Picture via"));
        }
        //Catch potential Exception thrown by getExternalCacheDir()
        catch (Exception e) {
            //Prince the stacktrace containing the exception
            e.printStackTrace();
        }
    }





    //'Screenshot' the View to share, and store it in a Bitmap object
    private Bitmap getBitmapFromView(View view) {

        //Create drawable
        Bitmap viewToShareBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);

        //Create a Canvas object, passing in the Bitmap version of the view to share
        Canvas canvas = new Canvas(viewToShareBitmap);

        //Create background drawable object
        Drawable backgroundDrawable = view.getBackground();

        //If background drawable EXISTS
        if (backgroundDrawable != null) {
            //Draw the background drawable on the draw on the canvas
            backgroundDrawable.draw(canvas);
        }
        //If background drawable does NOT exist
        else {
            //Draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }

        //Draw the canvas on the View
        view.draw(canvas);

        //Return view to share as a Bitmap
        return viewToShareBitmap;
    }





    //Open 'Update Location Confirmation' dialog - i.e. confirms the update of the Pix's location
    public void updateLocationConfirmationDialog(){

        //Create FragmentManager
        FragmentManager fragmentManager = getFragmentManager();

        //Create DatePickerFragment fragment
        //Argument 1: The STORED address line (i.e. the address line that is stored in the SQLiteDatabase for this Pix)
        //Argument 2: The CURRENT address line (i.e. the address line obtained from the location request, i.e. the address line of the CURRENT LOCATION)
        PixUpdateLocationConfirmationDialogFragment pixUpdateLocationConfirmation = PixUpdateLocationConfirmationDialogFragment.newInstance(mPix.getAddress(), mAddressOutput.getAddressLine(0));

        //Start the dialog fragment
        pixUpdateLocationConfirmation.setTargetFragment(PixDetailFragment.this, REQUEST_CODE_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION);

        //Show dialog
        pixUpdateLocationConfirmation.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION);
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

        //BELOW CODE from onCreate(..)
        // REPEATED AGAIN in onStart() so that when the user returns to the detail view from enabling locations (i.e. from the Update Locations Confirmation dialog),
        // the location could be resetted again.

        //_________ Request for (dangerous) PERMISSIONS from permission group: Location _________________

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

            //Try risky task - onAddSuccessListener could throw a SecurityException exception
            try {
                //Get most recent location available via getLastLocation(). NOTE: getLastLocation() runs ASYNCHRONOUSLY, whereas .
                mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {

                    //Listen for when location has been obtained via FusedLocationClient
                    @Override
                    public void onSuccess(Location location) {

                        //If location does not
                        if (location == null) {
                            //Log to Logcat
                            Log.v(TAG, "Location does not exist");
                        }

                        //Stash mLocation instance reference variable to local reference variable of location object
                        mLocation = location;

                        //In some RARE cases, location returned can be null
                        if (mLocation == null) {
                            //Exit from try-block
                            return;
                        }

                        //If Geocoder is NOT present (Geocoder: object to convert location fix (lat/lon values) to linguistic address - known as 'reverse geocoding')
                        if (!Geocoder.isPresent()){
                            //Toast to display that the Geocoder is NOT available
                            Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();

                            //Exit from try-block
                            return;
                        }

                        //Create ResultReceiver object, passing a Handler to it.
                        //NOTE: A ResultReceiver's purpose is to receive results from IntentService!
                        mAddressResultReceiver = new AddressResultReceiver(new Handler());

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



        //Declare LocationCallback method for updating the location
        //NOTE: Updating the location MUST be done AFTER a location has already been obtained (i.e. after reverse geocoding, as per above)
        mLocationCallback = new LocationCallback() {

            //Set listener for the callback
            @Override
            public void onLocationResult(LocationResult locationResult) {

                //If updated location does not
                if (locationResult == null) {
                    Log.v(TAG, "Updated location does not exist");
                }

                //Set mLocation instance variable to new Location object (i.e. the updated location)
                mLocation = locationResult.getLastLocation();

                //In some RARE cases, location returned can be null
                if (mLocation == null) {
                    return;
                }

                //If Geocoder is NOT present (Geocoder: object to convert location fix (lat/lon values) to linguistic address
                if (!Geocoder.isPresent()) {
                    Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                    return;
                }

                //Set mAddressResultReceiver instance variable to new AddressResultReceiver object
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

        //Stop location updates (i.e. so that the location update does not keep persisting)
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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

        //Nullify the callback interface reference variable
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
            //Get Date object from Date dialog fragment
            Date setDate = (Date) intent.getSerializableExtra(DatePickerFragment.EXTRA_DATE);

            //Set new date for Pix
            mPix.setDate(setDate);

            //Set new date display for date button
            mDateButton.setText(mDateFormat.format("EEE d MMMM yyyy", mPix.getDate()));

            //Update Pix (upon new date change)
            updatePix();
        }




        //If request code matches the 'Delete Confirmation' dialog's
        if (requestCode == REQUEST_CODE_DIALOG_FRAGMENT_DELETE_CONFIRMATION){
            //Get boolean object from 'Delete Confirmation' dialog fragment - i.e. whether the positive button was pressed
            boolean confirmDelete = intent.getBooleanExtra(PixDeleteConfirmationDialogFragment.EXTRA_PIX_DELETE_CONFIRMATION, false);

            //If the positive button was pressed - i.e. to confirm Pix delete
            if (confirmDelete == true) {
                //Delete Pix from SQLiteDatabase, "pixes"
                PixManager.get(getActivity()).deletePix(mPix);

                //Call callback method to remove PixDetailFragment fragment from the 2nd pane of PixlistActivity activity (if the view is in two-pane layout)
                mCallbacks.onPixDeleted(mPix);


                //Check if PixViewPagerActivity activity is running (i.e. we are in single-pane layout, i.e. sw < 600dp).
                // This check is important, since we want to close PixViewPagerActivity activity IF and ONLY IF it is running (i.e. in one-pane layout)...
                // This is because closing PixViewActivity would pop this activity off the back stack and return us to PixListActivity.
                // While in 2-pane layout however, the only activity running is PixListActivity (it hosts PixListFragment and PixDetailFragment).
                // We DO NOT want to 'finish()' PixListActivity, as doing so would close the whole activity, hence, the app.
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
            }

        }




        //If requestCode matches contact app's and the intent to open it exists (since a contact app may not exist or be resolved in the device)
        if (requestCode == REQUEST_CODE_CONTACT && intent != null) {
            //Get contact's URI from contact intent
            Uri contactUri = intent.getData();

            //Get display name
            String[] displayNameField = new String[]{ContactsContract.Contacts.DISPLAY_NAME};

            //Create cursor to query ContactsContract.Contacts table
            Cursor cursorDisplayName = getActivity().getContentResolver().query(
                    contactUri, //whereClause (identifies the value in the column, in order to get the corresponding ROW whereby to place the cursor on): URI of the contact to query)
                    displayNameField, //whereArgs (identifies the column): Contains display name of the contact to query
                    null,
                    null,
                    null
            );


            //Try 'risky' task, as setText(..) may throw RuntimeException (in the rare case that a contact's DISPLAY NAME does NOT exist)
            try {
                //Check if cursor contains results
                if (cursorDisplayName.getCount() == 0) {
                    return;
                }

                //Move cursor to first column/field in the identified ROW (i.e. the display name)
                //NOTE: This row is the row containing all fields corresponding to a SINGLE contact!
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
            }
            //If displayName does NOT exist
            catch(RuntimeException runtimeException){
                //Log to Logcat
                Log.i(TAG, "Contact display name does NOT exist");
            }
            finally {
                //Close cursor
                cursorDisplayName.close();
            }
        }




        //If resultCode matches camera activity
        if (requestCode == REQUEST_CODE_PICTURE_CAMERA) {
            //Get content URI of FileProvider for which picture file taken from camera has been saved
            Uri uriFileProvider = FileProvider.getUriForFile(
                    getActivity(), //Return the parent activity (i.e. PixViewPagerActivity or PixListActiivty)
                    "com.petertieu.android.mepix.fileprovider", //(String): The authority of the FileProvider - defined in <provider> element in Manifest
                    mPictureFile //(File): The picture file
            );

            //Revoke persmission from camera from writing to FileProvider
            getActivity().revokeUriPermission(uriFileProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            //Update picture view
            updatePictureView();
        }




//        //If reqsultCode matches gallery activity
//        if (requestCode == REQUEST_CODE_PICTURE_GALLERY) {
//
//            //Get content URI of FileProvider for which picture file taken from camera has been saved
//            Uri uriFileProvider = FileProvider.getUriForFile(
//                    getActivity(), //
//                    "com.petertieu.android.mepix.fileprovider", //(String): The authority of the FileProvider - defined in <provider> element in Manifest
//                    mPictureFile //(File): The picture file
//            );
//
//            Bitmap bm=null;
//            if (intent != null) {
//                try {
//                    bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), intent.getData());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            mPictureView.setImageBitmap(bm);
//
//            //Revoke permission from camera from writing to FileProvider
//            getActivity().revokeUriPermission(uriFileProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
////            //Update picture view
////            updatePictureView();
//
//            //Update Pix SQLiteDatabase and two-pane UI (upon any changes)
//            updatePix();
//        }




        //If resultcode matches 'Location Update Confirmation' dialog's
        if (requestCode == REQUEST_CODE_DIALOG_FRAGMENT_UPDATE_LOCATION_CONFIRMATION){
            //Get boolean object from 'Update Pix Location' confirmation dialog fragment - i.e., whether the positve button was pressed
            boolean confirmUpdatePix = intent.getBooleanExtra(PixUpdateLocationConfirmationDialogFragment.EXTRA_PIX_UPDATE_LOCATION_CONFIRMATION, false);

            //If the positive button was pressed - i.e. to confirm the update of the location of Pix
            if (confirmUpdatePix == true) {

                //Declare that the Locations Update menu item is pressed.
                // This is then used in an if-statement to reset the mLocationButton's display display (of the location)
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





        //If resultCode matches 'ImageViewDialogFragment's "Save Picture to Gallery" button's
        if (requestCode == REQUEST_CODE_PICTURE_SAVED_TO_GALLERY){
            //Get boolean object to indicate whether Picture has been saved to Gallery
            boolean pictureSavedToGallery = intent.getBooleanExtra(ImageViewDialogFragment.EXTRA_PIX_PICTURE_SAVED_TO_GALLERY, false);

            //If Picture has been saved to Gallery
            if (pictureSavedToGallery) {
                //Display toast for successful save
                Toast.makeText(getActivity(), "Picture saved to Gallery", Toast.LENGTH_LONG).show();

                //======= Hide soft keyboard (if it is on the screen) ========
                //Get InputMethodManager object
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                //Request to hide soft keyboard. Argument 1 (IBinder): Any view visible on screen (e.g. mTitle)
                inputMethodManager.hideSoftInputFromWindow(mFavoritedButton.getWindowToken(), 0);
            }
        }

    }





    //======================== Define HELPER METHODS used in onActivityResult(..) =============================================
    //Request for NEW location. This assumes a location already exists via the reverse geocoding procedure
    protected void createLocationRequest() {

        //Create LocationRequest object to set location update properties from FusedLocationProviderApi
        mLocationRequest = new LocationRequest();

        //Set interval (in ms) to request location update
        mLocationRequest.setInterval(10000);

        //Set fastest interval (in ms) to request location updates
        mLocationRequest.setFastestInterval(5000);

        //Set priority of the location update
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }





    //Update location of Pix, assuming a location fix already exists (i.e. from reverse geocoding) so that mLocatoinCallback could exist
    //NOTE: This method is the CRUX of location updates, since it does the actual work of requesting for the location updates!
    // IOW, the very heart of location update is in the method: requestLocationUpdates(LocationRequest, LocationCallback, Looper)
    private void updatePixLocation(){

        //Try 'risky' task - onAddSuccessListener from mFusedLocationClient.getLastLocation() could throw a SecurityException exception.
        //NOTE: mFusedLocationClient.getLastLocation() is required to obtain mLocationCallback (in onCreate(..))
        try{
            //Request for location update
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
        catch (SecurityException securityException){
            //Log in Logcat
            Log.e(TAG, "Error requesting location updates", securityException);
        }
    }

}
