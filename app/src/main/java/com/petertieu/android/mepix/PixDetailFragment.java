package com.petertieu.android.mepix;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.Toast;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Peter Tieu on 14/12/2017.
 */



//Fragment for the DETAIL VIEW
public class PixDetailFragment extends Fragment {

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
    private Button mPictureButton; //Photo Button
    private Button mLocationButton; //Location Button
    private EditText mDescription; //Description Button


    //Declare DateFormat object for formatting the date display
    private DateFormat mDateFormat;
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_DATE = 0;  //Request code for receiving results from dialog fragment
    private static final String IDENTIFIER_DIALOG_FRAGMENT_DATE = "DialogDate"; //Identifier of dialog fragment

    //Declare constants for delete confirmation dialog
    private static final int REQUEST_CODE_DIALOG_FRAGMENT_DELETE = 0; //Request code for receiving results from dialog fragment
    private static final String IDENTIFIER_DIALOG_FRAGMENT_DELETE = "DialogDelete"; //Identifier of dialog fragment

    //Declare constants for tag requests
    private static final int REQUEST_CODE_CONTACT = 1;


    //Declare Callbacks interface reference variable
    private Callbacks mCallbacks;





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
        void onPixUpdated(Pix pix);

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

        //Log lifecycle callback
        Log.i(TAG, "onCreate(..) called");

        //Get the 'value' of the argument-bundle
        UUID pixId = (UUID) getArguments().getSerializable(ARGUMENT_PIX_ID);

        //Create Pix object
        mPix = new Pix();

        //Assign Pix object to Pix object from PixManager singleton
        mPix = PixManager.get(getActivity()).getPix(pixId);

        //Declare an options menu for the fragment
        setHasOptionsMenu(true);

    }





    //Override onCreateView(..) fragment lifecycle callback method
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle){
        super.onCreateView(layoutInflater, viewGroup, bundle);

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
//        mPictureButton = (Button) view.findViewById(R.id.detail_pix_add_picture);

//        mPictureButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                final CharSequence[] dialogPictureItems = {"Take Photo", "Choose from Library", "Cancel"};
//
//                AlertDialog pictureAlertDialog = new AlertDialog()
//                        .Builder(getActivity())
//                        .setTitle("Add Picture")
//                        .setItems(dialogPictureItems, new DialogInterface.OnClickListener(){
//                            @Override
//                            public void onClick(DialogInterface dialog, int item){
//                                boolean result = Utility.checkPermission().getActivity
//
//                    }
//                })
//
//            }
//        });












        //Return the View
        return view;
    }




    //Update Pix SQLiteDatabase and two-pane UI (upon any changes)
    private void updatePix(){

        //Update the SQLite database based on the Pix passed
        PixManager.get(getActivity()).updatePixOnDatabase(mPix);

        //Update PixListFragment() in 'real-time' for two-pane layout
        mCallbacks.onPixUpdated(mPix);
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent){

        //If a result code DOES NOT exist
        if (resultCode != Activity.RESULT_OK){
            return;
        }


        //If request code matches the date dialog fragment's
        if (requestCode == REQUEST_CODE_DIALOG_FRAGMENT_DATE){

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
        if (requestCode == REQUEST_CODE_CONTACT && intent != null){

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
                if (!mTotalTag.isEmpty()){
                    //Append total Pix tag String to display name of contact
                    mTotalTag = mTotalTag + ",\n" + displayName;
                }
                //If total Pix tag String is empty initially (i.e. no tags made, i.e. EditText not filled)
                else{
                    //Let total Pix tag String equal display name of contact
                    mTotalTag = displayName;
                }


                //Set tag field of Pix to obtained data
                mPix.setTag(mTotalTag);

                //Display obtained data to mTagButton
//                mTagButton.setText(displayName);

                mTagEditText.setText(mPix.getTag());

                //Update the Pix SQLiteDatabase and two-pane UI (upon change with Pix's tag field)
                updatePix();
            }

            finally{
                //Close curosr
                cursorDisplayName.close();
            }
        }

    }


}
