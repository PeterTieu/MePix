package com.petertieu.android.mepix;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




//Fragment for the LIST VIEW
public class PixListFragment extends Fragment{

    //Define Log identifier
    private final String TAG = "PixListFragment";

    //Declare the RecyclerView instance variable
    private RecyclerView mPixRecyclerView;

    //Declare Adapter instance variable
    private PixAdapter mPixAdapter;

    //Declare ViewHolder instance variable
    PixViewHolder mPixViewHolder;

    //Declare "no pixes view" layout
    private LinearLayout mNoPixView;

    //Declare "add new pix" button
    private Button mAddNewPix;

    //Declare picture File
    private File mPictureFile;

    //Declare Callbacks interface reference variable
    private Callbacks mCallbacks;

    //Identifier of dialog fragment of picture ImageView
    private static final String IDENTIFIER_DIALOG_FRAGMENT_PICTURE = "IdentifierDialogFragmentPicture";

    //List locations permissions required
    private static final String[] LOCATION_PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final String[] STORAGE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    //Define request codes
    private static final int REQUEST_CODE_FOR_LOCATION_PERMISSIONS = 0; //Request code for location fix
    private static final int REQUEST_CODE_FOR_STORAGE_PERMISSIONS = 1; //Request code to WRITE to external storage
    private static final int REQUEST_CODE_PICTURE_VIEW_DIALOG_FRAGMENT = 2; //Request code for receiving results form dialog fragment to Pix picture being saved to gallery





    //Declare callbacks interface
    interface Callbacks{

        //Calback method for when a Pix is selected in list view by either:
        // 1: NEW Pix created (via the "Add New Pix" button on the toolbar
        //  OR
        // 2: EXISTING Pix selected in the list view
        void onPixSelected(Pix pix);

        //Callback method for when Pix is CHANGED in list view..., i.e. by: toggling of "favorite" status via pressing onto the 'star'.
        //This method is called whenever this changes happen - SO THAT when in the two-pane layout/mode, the detail-view would be changed
        // simultaneously to a change in the list view
        void onPixUpdatedFromListView(Pix pix);
    }





    //Override onAttach(..) fragment lifecycle callback method
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        //Log in Logcat
        Log.i(TAG, "onAttach() called");

        //Declare mCallbacks ref. var. (to PixListFragment.Callbacks)
        mCallbacks = (Callbacks) context;
    }





    //Override onCreate(..) fragment lifecycle callback method
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Log lifecycle callback
        Log.i(TAG, "onCreate(..) called");

        //Report that this fragment would like to participate in populating menus
        setHasOptionsMenu(true);

        //Reset options menu
        getActivity().invalidateOptionsMenu();

        //If permission for location tracking has NOT been granted at runtime (by user)
        if (hasLocationPermission() == false){
            //Request (user) for location permissions - as they are 'dangerous' permissions (and therefore must be requested)
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_CODE_FOR_LOCATION_PERMISSIONS);
        }
        //If permission for location tracking HAS been granted at runtime (by user)
        if (hasWriteExternalStoragePermission() == false){
            //Request (user) for storage permissions - as they are 'dangerous' permissions (and therefore must be requested)
            requestPermissions(STORAGE_PERMISSIONS, REQUEST_CODE_FOR_STORAGE_PERMISSIONS);
        }
    }





    //Check if LOCATION permissions have been granted at runtime (by user)
    private boolean hasLocationPermission(){

        //If permission is granted, result = PackageManager.PERMISSION_GRANTED (else, PackageManager.PERMISSION_DENIED).
        //NOTE: Permissions ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION are in the same PERMISSION GROUP, called ADDRESS.
        //If one permission is a permission group is granted/denied access, the same applies to all other permissions in that group.
        // Other groups include: CALENDAR, CAMERA, CONTACTS, MICROPHONE, PHONE, SENSORS, SMS, STORAGE.
        int result = ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);

        //Return a boolean for state of location permission
        return (result == PackageManager.PERMISSION_GRANTED);
    }





    //Check if STORAGE permissions have been granted at runtime (by user)
    private boolean hasWriteExternalStoragePermission(){

        //If permission is granted, result = PackageManager.PERMISSION_GRANTED (else, PackageManager.PERMISSION_DENIED).
        //NOTE: Permissions WRITE_EXTERNAL_STORAGE and READ_EXTERNAL_STORAGE are in the same PERMISSION GROUP, called STORAGE.
        //If one permission is a permission group is granted/denied access, the same applies to all other permissions in that group.
        // Other groups include: CALENDAR, CAMERA, CONTACTS, MICROPHONE, PHONE, SENSORS, SMS, STORAGE.
        int result = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (result == PackageManager.PERMISSION_GRANTED);
    }





    //Override onStart() fragment lifecycle callback method
    @Override
    public void onStart(){
        super.onStart();

        //If list items are present (i.e. if the screen is NOT blank, meaning the the mNoPixView is NOT visible)
        if (PixManager.get(getActivity()).getPixes().size() > 0) {
            //Report that this fragment would like to participate in populate menus
            setHasOptionsMenu(true);
        }
        //If list items are NOT present (i.e. if the screen IS blank, whereby the mNoPixView IS visible)
        else{
            setHasOptionsMenu(false);
        }

        //Log lifecycle callback
        Log.i(TAG, "onStart() called");
    }





    //Override onResume() fragment lifecycle callback method
    @Override
    public void onResume(){
        super.onResume();

        //Log lifecycle callback
        Log.i(TAG, "onResume() called");

        //Create/call the Adapter and link it with the RecyclerView
        updateUI();
    }





    //Override onCreateView(..) fragment lifecycle callback method
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState){
        super.onCreateView(layoutInflater, viewGroup, savedInstanceState);

        //Log lifecycle callback
        Log.i(TAG, "onCreateView(..) called");

        //Create View object
        View view = layoutInflater.inflate(R.layout.fragment_pix_list, viewGroup, false);

        //Create RecyclerView for the LIST VIEW
        mPixRecyclerView = (RecyclerView) view.findViewById(R.id.pix_list_recycler_view);

        //Set layout for the RecyclerView
        mPixRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        //Add decoration to list items (i.e. add dividers)
        mPixRecyclerView.addItemDecoration(new PixItemDecoration(getActivity()));


        //======= Add layout for when the screen is blank (i.e. no list items are present) =========================================
        //Create view for "no pix view", i.e. view before any pix is added
        mNoPixView = (LinearLayout) view.findViewById(R.id.no_pixes_view);

        //Create button for adding first Pix
        mAddNewPix = (Button) view.findViewById(R.id.add_new_pix);

        //Add listener to button to add first pix
        mAddNewPix.setOnClickListener(new View.OnClickListener(){

            //Override method from View.OnClickListener interface
            @Override
            public void onClick(View view){
                //Create new Pix
                Pix pix = new Pix();

                //Add new Pix to SQLiteDatabase, "pixes"
                PixManager.get(getActivity()).addPix(pix);

                //Call callback method from PixListFragment.Callbacks
                mCallbacks.onPixSelected(pix);

                //Display toast to notify user a new pix has been added
                Toast.makeText(getActivity(), R.string.new_pix_added, Toast.LENGTH_LONG).show();
            }
        });

        //Create/call the Adapter and link it with the RecyclerView
        updateUI();


        return view;
    }





    //Helper method for creating and setting the list view, including setting "Add New Pix" button and set up Adapter and linking it with the RecyclerView
    public void updateUI(){

        //Assign the mPixes reference variable to the List of Pix objects from the PixManager singleton
        final List<Pix> mPixes = PixManager.get(getActivity()).getPixes();


        //============ Set visibility of "no pix view" - if no Pixes exist ==============================
        //If there are no Pixes present (i.e the list view is empty - no list items)
        if (mPixes.size() == 0){
            //Set the mNoPixView to be visible
            mNoPixView.setVisibility(View.VISIBLE);
        }
        //If there are Pixes present (i.e. the lsit view is populated by >1 list item)
        else{
            //Set the mNoPixView to NOT be visible
            mNoPixView.setVisibility(View.GONE);
        }


        //============ Set up Adapter ==============================
        //If an Adapter does NOT exist...
        if (mPixAdapter == null){
            //Create new Adapter
            mPixAdapter = new PixAdapter(mPixes);

            //Set the Adapter to the RecyclerView
            mPixRecyclerView.setAdapter(mPixAdapter);
        }
        //If an Adapter EXISTS...
        else{
            //Update the Adapter with a new List of Pix objects (i.e. updated List for after a Pix has been created)
            mPixAdapter.setPixes(mPixes);

            //Invalidate the previous Adapter version, and update it
            mPixAdapter.notifyDataSetChanged();
        }
    }





    //Override onCreateOptionsMenu(..) fragment lifecycle callback method
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu, menuInflater);

        //Log lifecycle callback
        Log.i(TAG, "onCreateOptionsMenu(..) called");

        //If there are one or more Pixes (i.e. one or more list items)
        if (PixManager.get(getActivity()).getPixes().size() > 0) {
            //Inflate a menu hierarchy from specified resource
            menuInflater.inflate(R.menu.fragment_pix_list, menu);
        }
    }





    //Override onOptionsItemSelected(..) fragment lifecycle callback method
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        //Log lifecycle callback
        Log.i(TAG, "onOptionsItemsSelected(..) called");

        //Check through all menuItems
        switch(menuItem.getItemId()){

            //Check the "New Pix" menu item
            case(R.id.new_pix):
                //Create a Pix object
                Pix pix = new Pix();

                //Add the Pix object to the SQLiteDatabase, "pixes"
                PixManager.get(getActivity()).addPix(pix);

                //Create/call the Adapter and link it with the RecyclerView - i.e. create a new Pix item inb the list view
                updateUI();

                //Open up the Pix (just created)
                mCallbacks.onPixSelected(pix);

                //Display toast to notify user a new pix has been added
                Toast toast = Toast.makeText(getActivity(), R.string.new_pix_added, Toast.LENGTH_LONG); //Create Toast
                toast.setGravity(Gravity.CENTER, 0,150); //Set positoin of Toast
                toast.show(); //Show Toast

                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }





    //Define the Adapter class
    private class PixAdapter extends RecyclerView.Adapter<PixViewHolder>{

        //Declare List of Pix objects
        private List<Pix> mPixes;



        //Build constructor
        public PixAdapter(List<Pix> pixes){
            //Assign the mPixes field
            mPixes = pixes;
        }



        //Override method from the RecyclerView.Adapter inner class of Adapter
        @Override
        public int getItemCount(){
            //Return number of items in the Adapter
            return mPixes.size();
        }



        //Override method from the RecyclerView.Adapter inner class of Adapter
        @Override
        public PixViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){

            //Create LayoutInflater from the given Context
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            //Inflate the View
            View view = layoutInflater.inflate(R.layout.list_item_pix, viewGroup, false);

            //Pass the inflated View into the ViewHolder constructor
            mPixViewHolder = new PixViewHolder(view);

            //Return the PixViewHolder object
            return mPixViewHolder;
        }



        //Override method from the RecyclerView.Adapter inner class of Adapter
        @Override
        public void onBindViewHolder(PixViewHolder pixViewHolder, int position){

            //Get a specific Pix object, 'pix', from the List of Pix objects, 'mPixes'
            Pix pix = mPixes.get(position);

            //Pass the Pix object to the bind(Pix) method of the ViewHolder
            pixViewHolder.bind(pix);
        }



        //Update the list of Pix objects for the 'mPixes' instance variable
        public void setPixes(List<Pix> pixes){
            mPixes = pixes;
        }

    }





    //Define the ViewHolder class
    private class PixViewHolder extends RecyclerView.ViewHolder{

        //Declare the Pix instance variable
        private Pix mPix;

        //Declare the list item's title instance variable
        private TextView mPixTitle;

        //Declare the list item's description instance variable
        private TextView mPixDescription;

        //Declare the list item's date instance variable
        private TextView mPixDate;

        //Declare DateFormat for formatting date display
        private DateFormat mPixDateFormat;

        //Declare ImageView for "Favourited" field
        private ImageView mPixFavorited;

        //Declare ImageView for "Picture" of Pix
        private ImageView mPictureView;

        //Declare TextView for "Address" of Pix
        private TextView mPixAddress;

        //Declare TextView for "Tag" of Pix
        private TextView mPixTagged;



        //Build constructor #1 - to be called by mPixAdapter's onCrateViewHolder(..) method
        public PixViewHolder(View view){
            super(view);

            //Assign the list item's title instance variable to its associated resource ID
            mPixTitle = (TextView) view.findViewById(R.id.list_pix_title);

            //Assign the list item's description instance variable to its associated resource ID
            mPixDescription = (TextView) view.findViewById(R.id.list_pix_description);

            mPixFavorited = (ImageView) view.findViewById(R.id.list_pix_favorited);

            mPixDate = (TextView) view.findViewById(R.id.list_pix_date);

            mPixFavorited = (ImageView) view.findViewById(R.id.list_pix_favorited);

            //Assign list item's picture instance variable to its associated resource ID
            mPictureView = (ImageView) view.findViewById(R.id.list_pix_picture);

            //Assign list item's location instance variable to its associated resource ID
            mPixAddress = (TextView) view.findViewById(R.id.list_pix_location);

            //Assign list item's tag instance variable to its associated resource ID
            mPixTagged = (TextView) view.findViewById(R.id.list_pix_tagged);


            //Set listener for click on the list item
            view.setOnClickListener(new View.OnClickListener(){

                //Override method of the View.OnClickListener interface of View
                @Override
                public void onClick(View view){

                    //Open up the Pix (existing)
                    mCallbacks.onPixSelected(mPix);
                }
            });


            //Set listener to open picture ImageView in list item view hierarchy
            mPictureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Get picture File of Pix
                    mPictureFile = PixManager.get(getActivity()).getPictureFile(mPix);

                    //Check if picture File is empty. NOTE: A File exists for each Pix, but has length either >0 OR 0.
                    // If File is not empty (i.e. contains .jpg), it has length > 0. If File is empty (i.e. does not contain .jpg), it has length 0.
                    //NOTE: This check is important, as the app would crash when the ImageView is pressed on IF it has no .jpg (i.e. the Pix has an empty picture File)
                    //NOTE: All ImageView objects that do NOT contain a picture, when pressed on, will DO NOTHING
                    if (mPictureFile.length() != 0) {

                        //Open picture view dialog
                        PictureViewDialogFragment pictureViewDialog = PictureViewDialogFragment.newInstance(mPictureFile, mPix.getTitle(), mPix.getDate());

                        //Set PixDetailFragment as target fragment for the dialog fragment
                        pictureViewDialog.setTargetFragment(PixListFragment.this, REQUEST_CODE_PICTURE_VIEW_DIALOG_FRAGMENT);

                        //Create FragmentManager (which has access to all fragments)
                        FragmentManager fragmentManager = getFragmentManager();

                        //Show the fragment
                        pictureViewDialog.show(fragmentManager, IDENTIFIER_DIALOG_FRAGMENT_PICTURE);
                    }

                }
            });


            //Set listener for "favorite" star in the list item's view hierarchy, so the user could toggle the "favorite" status of the Pix in the list view
            mPixFavorited.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){

                    //Get Pix's current boolean state of "favorite"
                    boolean currentFavoritedState = mPix.isFavorited();

                    //Reverse the "favorite" state
                    mPix.setFavorited(!currentFavoritedState);

                    //Update Pix's SQLiteDatabase to account for new "favorite" state
                    updatePix();
                }
            });

        }



        //Update Pix SQLiteDatabase and two-pane UI (upon any changes)
        private void updatePix(){

            //Update the SQLite database based on the Pix passed
            PixManager.get(getActivity()).updatePixOnDatabase(mPix);

            //Update list view
            updateUI();

            //If the two-pane mode/layout is active (i.e. sw > 600dp)
            if (getActivity().findViewById(R.id.detail_fragment_container) != null) {

                //Update PixListFragment() in 'real-time' for two-pane layout
                mCallbacks.onPixUpdatedFromListView(mPix);
            }
        }



        //Stash the Pix object sent from the Adapter - to be called by mPixAdapter's onBineViewHolder(..) method
        public void bind(Pix pix){

            //Assign the Pix instance variable to that sent from the Adapter
            mPix = pix;

            //If the title of the Pix does NOT exist or only has spaces
            if (mPix.getTitle() == null || mPix.getTitle().isEmpty() || mPix.getTitle().trim().length()==0){
                //Set curentDescriptionEditTextString of the list item's title
                mPixTitle.setText("* Untitled *");

                //Set colour of the list item's title
                mPixTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.yellow));

                //Set type-face of the list item's title
                mPixTitle.setTypeface(null, Typeface.ITALIC);
            }
            //If the title of the Pix DOES exist
            else{
                //Set curentDescriptionEditTextString of the list item's title to title of the Pix
                mPixTitle.setText(mPix.getTitle());

                //Set color of the list item's title
                mPixTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.dark_gray));

                //If two-pane view/layout/mode is present (i.e. sw > 600dp)
                if(getActivity().findViewById(R.id.detail_fragment_container) != null){
                    //Set to the max-width of the title to a lower amount, since there would be less space for it to fit into the view
                    // in two-pane view, as opposed to one-pane view
                    mPixTitle.setMaxWidth(440);
                }
            }


            //If the description of the Pix does NOT exist or only has spaces
            if (mPix.getDescription() == null || mPix.getDescription().isEmpty() || mPix.getDescription().trim().length()==0){
                //Set curentDescriptionEditTextString of the list item's description
                mPixDescription.setText("* No description *");

                //Set type-face of the list item's description
                mPixDescription.setTypeface(null, Typeface.ITALIC);

                //Set color of the list item's description
                mPixDescription.setTextColor(ContextCompat.getColor(getActivity(), R.color.dark_yellow));
            }
            //If the description of the Pix DOES exist
            else{
                //Set curentDescriptionEditTextString of the list item's description
                mPixDescription.setText(mPix.getDescription());

                //If the view is in one-pane mode
                if(getActivity().findViewById(R.id.detail_fragment_container) != null){
                    //Set to the max-width of the description to a lower amount, since there would be less space for it to fit into the view
                    // in two-pane view, as opposed to one-pane view
                    mPixDescription.setMaxWidth(440);
                }

                if (mPixTagged == null || mPixTagged.toString().isEmpty()){
                    mPixDescription.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    mPixDescription.setMaxLines(2);
                }
            }



            //Set new date display for date button
            mPixDate.setText(mPixDateFormat.format("EEE d MMM yy", mPix.getDate()));

            //If the Pix's address does NOT exist
            if (mPix.getAddress() == null || mPixAddress.toString().isEmpty()){
                //Display nothing on location field
                mPixAddress.setText("");
            }
            //If the Pix's address DOES exist
            else{
                //Display locality on location field (i.e. Footscray, Victoria)
                mPixAddress.setText("- in " + mPix.getLocality());
            }



            //If the Pix's tag field does NOT exist
            if(mPix.getTag() == null || mPix.getTag().isEmpty()){
                //Set curentDescriptionEditTextString for the Pix's tag field
                mPixTagged.setText("");
            }
            //If the Pix's tag field DOES exist
            else{
                //Get tag String (which contains the newline character, "\n")
                String pixTaggedStringFromDetailView= mPix.getTag();

                //Create ArrayList from tag String, each being separated by the newline character and a hyphen ("\n-")
                //NOTE: Each String in the ArrayList resembles a tagged contact
                List<String> pixTaggedList = new ArrayList<String> (Arrays.asList(pixTaggedStringFromDetailView.split("\n-")));

                //If there were more than one tagged contact
                if(pixTaggedList.size() > 1){
                    //Add the " and" String into the ArrayList after the 2nd last contact and before the last contact
                    pixTaggedList.add(pixTaggedList.size()-1, " and");
                }


                //Declare String to contain all Strings from the pixTaggedStringForListView ArrayList
                String pixTaggedString = "";

                //If more than one contact were tagged
                if (pixTaggedList.size() > 1){
                    //Cycle through all String objects in the pixTaggedList ArrayList
                    for (int i=0; i<pixTaggedList.size(); i++){
                        //Concatenate all String objects in the pixTaggedList ArrayList to pixTaggedString String
                        pixTaggedString += pixTaggedList.get(i);
                    }
                }
                //If only one contact was tagged
                else{
                    //Store that one contact from the pixTaggedList ArrayList to the pixTaggedString String
                    pixTaggedString = pixTaggedList.get(0);
                }


                //Add "- with" before the pixTaggedString String
                pixTaggedString = "- with " + pixTaggedString;
                //Since there will be a "-" before the first contact, replace the "- with-" with "- with"
                pixTaggedString = pixTaggedString.replaceAll("- with -", "- with");
                //Replace the ", and" with "and" (effectively removing the comma before " and")
                pixTaggedString = pixTaggedString.replaceAll(", and", " and");


                //Display pixTaggedString in the mPixTagged Button
                mPixTagged.setText(pixTaggedString);
            }



            //If layout is in two-pane (i.e. sw > 600dp)
            if (getActivity().findViewById(R.id.detail_fragment_container) != null){
                //Remove curentDescriptionEditTextString display of address
                mPixAddress.setText("");
                //Remove curentDescriptionEditTextString display of tagged contacts
                mPixTagged.setText("");
            }



            //============ Set Favorited 'star' view===================================
            //Resource ID for favorited drawable ('yellow' star)
            int favoritedDrawable = getResources().getIdentifier("android:drawable/btn_star_big_on", null, null);

            //Resource ID for non-faovirted drawable ('grey' star)
            int nonFavoritedDrawable = getResources().getIdentifier("android:drawable/btn_star_big_off", null, null);

            //If: mPix.isFavorited() == true.. then: mPixFavorited.setImageResource(favoriteDrawable).. else: mPixFavorited.setImageResource(nonFavoriteDrawable)
            mPixFavorited.setImageResource(mPix.isFavorited() ? favoritedDrawable : nonFavoritedDrawable);


            //============ Set Picture view===================================
            //If picture exists
            if (mPix.getPictureFilename() != null){

                //Get picture File of Pix
                mPictureFile = PixManager.get(getActivity()).getPictureFile(mPix);

                //Update picture view
                updatePictureView();
            }
        }



        //Update picture view
        private void updatePictureView(){

            //If picture file does NOT exist
            if (mPictureFile == null || !mPictureFile.exists()){
                //Talkback accessibility: Associate textual description to 'empty' view
                mPictureView.setContentDescription(getString(R.string.pix_no_picture_description));

                //Set the Btimap image of mPictureView to the default picture
                //NOTE: This line may look redundant, since the default picture of mPictureView is already R.drawable.pix_default_picture...
                // However, this actually fixes the bug whereby mPictureView views with Pix pictures sometimes get copied over to empty mPictureViews (i.e. ones without Pix picture)
                mPictureView.setImageDrawable(getResources().getDrawable(R.drawable.pix_default_picture));
            }

            //If picture file EXISTS
            else{
                //Get picture in bitmap 'scaled' format
                Bitmap pictureBitmap = PictureUtility.getScaledBitmap(mPictureFile.getPath(), getActivity());


                //==========Rotate picture bitmap to correct orientation - as pictureBitmap is 90 degrees off-rotation============

                //Create Matrix object for transforming co-ordinates
                Matrix matrix = new Matrix();

                //Set rotation for Matrix
                matrix.postRotate(90);

                //Rotate picture Bitmap
                Bitmap pictureBitmapCorrectOrientation = Bitmap.createBitmap(pictureBitmap , 0, 0, pictureBitmap .getWidth(), pictureBitmap.getHeight(), matrix, true);

                //Set picture ImageView view to bitmap version
                mPictureView.setImageBitmap(pictureBitmapCorrectOrientation);

                //Scale picture to fit allocated ImageView space
                mPictureView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                //Talkback accessibility: Associate textual description to 'existing' view
                mPictureView.setContentDescription(getString(R.string.pix_picture_description));
            }
        }

    }





    //Define ItemDecoration class - a class that allows manipulation of list item interface
    //Purpose: To add divider (lines) to list items
    //NOTE: This class will be created in: mPixRecyclerView.addItemDecoration(new PixItemDecoration(getActivity()));
    public class PixItemDecoration extends RecyclerView.ItemDecoration {

        //Declare Drawable interface for dividers
        private Drawable mDivider;

        //Declare Drawable interface for dividers
        private Drawable mRightLine;

        //Build constructor
        public PixItemDecoration(Context context) {
            //Get Drawable instance variable from Context
            mDivider = ContextCompat.getDrawable(context, R.drawable.recyclerview_divider);

            //Get Drawable instance variable from Context
            mRightLine = ContextCompat.getDrawable(context, R.drawable.two_pane_divider);
        }

        //Override callback method.
        // Draw decorations into the Canvas supplied to the RecyclerView.
        // Drawing will occur AFTER the item views are drawn and will thus appear OVER the views.
        //Purpose: To add dividers to each RecyclerView list item
        @Override
        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {

            //Get LEFT horizontal bound of recycler view divider (recyclerView.getPaddingLeft() will equal 0 if no padding were applied)
            int dividerLeftBound = recyclerView.getPaddingLeft();
            //Get RIGHT horizontal bound of divider
            int dividerRightBound = recyclerView.getWidth() - recyclerView.getPaddingRight();


            //Get LEFT horizontal bound of recycler view divider line
            int twoPaneDividerLineLeftBound = recyclerView.getWidth()-7;
            //Get RIGHT horizontal bound of divider line
            int twoPaneDividerLineRightBound = recyclerView.getWidth();


            //Get number of View children in the RecyclerView. Views might be: Bitmap, TextView, etc.
            int viewChildrenOfListItemCount = recyclerView.getChildCount();


            //Cycle over all View children in the RecyclerView
            for (int i = 0; i < viewChildrenOfListItemCount; i++) {
                //Get the View
                View viewChildOfListItem = recyclerView.getChildAt(i);

                //Get the LayoutParams associated with this view. All views should have layout parameters.
                // Layout parameters supply parameters to the parent of this view specifying how it should be arranged.
                RecyclerView.LayoutParams listItemLayoutParams = (RecyclerView.LayoutParams) viewChildOfListItem.getLayoutParams();


                //Get TOP bound of recycler view divider
                int dividerTopBound = viewChildOfListItem.getBottom() + listItemLayoutParams.bottomMargin;
                //Get BOTTOM bound of recycler view  divider
                int dividerBottomBound = dividerTopBound + mDivider.getIntrinsicHeight();


                //Get TOP horizontal bound of divider line
                int twoPaneDividerLineTopBound = viewChildOfListItem.getTop();
                //Get BOTTOM horizontal bound of divider line
                int twoPaneDividerLineBottomBound = getView().getBottom();


                //Set bounds for where the recycler view divider (lines) are to appaer
                mDivider.setBounds(dividerLeftBound, dividerTopBound, dividerRightBound, dividerBottomBound);

                //Draw the recycler view divider (lines) inside set bounds
                mDivider.draw(canvas);


                //If the view is in two-pane mode (i.e. sw > 600dp)
                if (getActivity().findViewById(R.id.detail_fragment_container) != null) {
                    //Set bounds for where the two-pane divider (lines) are to appear
                    mRightLine.setBounds(twoPaneDividerLineLeftBound, twoPaneDividerLineTopBound, twoPaneDividerLineRightBound, twoPaneDividerLineBottomBound);

                    //Draw the two-pane divider (lines) inside set bounds
                    mRightLine.draw(canvas);
                }

            }
        }

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

        //If resultCode matches 'PictureViewDialogFragment's "Save Picture to Gallery" button's
        if (requestCode == REQUEST_CODE_PICTURE_VIEW_DIALOG_FRAGMENT){
            //Get boolean object to indicate whether the "Save to Gallery" button has been pressed
            boolean pictureSavedToGallery = intent.getBooleanExtra(PictureViewDialogFragment.EXTRA_PIX_PICTURE_SAVED_TO_GALLERY, false);

            //Get boolean object to indicate whether the "Delete Picture" button has been pressed
            boolean pictureDeleteSelected = intent.getBooleanExtra(PictureViewDialogFragment.EXTRA_PIX_PICTURE_DELETE_SELECTED, false);

            //Result returned from pressing the "Save Picture to Gallery" button (i.e. result returned indicating whether the picture has been saved to the gallery)
            if (pictureSavedToGallery) {
                //Display toast for successful save
                Toast.makeText(getActivity(), "Picture saved to Gallery", Toast.LENGTH_LONG).show();
            }

            //Result returned from pressing the "Delete Pix" button
            if (pictureDeleteSelected){

                //Delete the picture File from the FileProvider
                mPictureFile.delete();

                //Set the mPictureView View (back) to the 'default' picture
                mPixViewHolder.mPictureView.setImageDrawable(getResources().getDrawable(R.drawable.pix_default_picture));


                //Display Toast to notify that the picture has been deleted
                Toast.makeText(getActivity(), "Picture Deleted", Toast.LENGTH_LONG).show();

                //Update the Pix - but this to simultateously update the DETAIL view (if sw>600dp, i.e. we are in two-pane layout/mode) using the callback method inside updatePix() (onPixUpdatedFromListView(Pix))
                mPixViewHolder.updatePix();
            }

        }

    }





    //Override onPause() fragment lifecycle callback method
    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause() called");
    }





    //Override onStop() fragment lifecycle callback method
    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop() called");
    }





    @Override
    public void onDestroyView(){
        super.onDestroyView();
        Log.i(TAG, "onDestroyView() called");
    }





    //Override onDestroy() fragment lifecycle callback method
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
    }





    //Override onDetach() fragment lifecycle callback method
    @Override
    public void onDetach(){
        super.onDetach();

        //Log in Logcat
        Log.i(TAG, "onDetach() called");

        //Null mCallbacks ref. var. (to PixListFragment.Callbacks object)
        mCallbacks = null;
    }

}
