package com.petertieu.android.mepix;

import android.content.Context;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.support.v7.widget.helper.ItemTouchHelper.Callback.makeMovementFlags;


/**
 * Created by Peter Tieu on 7/12/2017.
 */

//Fragment for the LIST VIEW
public class PixListFragment extends Fragment{

    //Define Log identifier
    private final String TAG = "PixListFragment";

    //Declare the RecyclerView instance variable
    private RecyclerView mPixRecyclerView;

    //Declare Adapter instance variable
    private PixAdapter mPixAdapter;

    //Declare "no pixes view" layout
    private LinearLayout mNoPixView;

    //Declare "add new pix" button
    private Button mAddNewPix;

    //Declare Callbacks interface reference variable
    private Callbacks mCallbacks;

    //Declare picture File
    private File mPictureFile;

    //Declare picture ImageView
    private ImageView mPictureView;

    //Identifier of dialog fragment of picture ImageView
    private static final String IDENTIFIER_DIALOG_FRAGMENT_PICTURE = "IdentifierDialogFragmentPicture";

    //List locations permissions required,
    private static final String[] LOCATION_PERMISSIONS = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_CODE_FOR_LOCATION_PERMISSIONS = 0; //Request code for location fix






    //Declare callbacks interface
    interface Callbacks{

        //Calback method for when a Pix is selected in list view.. by either: New Pix created OR Pix selected in the list view
        void onPixSelected(Pix pix);

        //Callback method for when Pix is changed in list view... by: toggling of "favorite" status via pressing onto the 'star'
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

        //Report that this fragment would like to participate in populate menus
        setHasOptionsMenu(true);

        getActivity().invalidateOptionsMenu();

        //Check if permission for location tracking has been granted (by user)
        if (hasLocationPermission() == false){
            //Request (user) for location permissions - as they are 'dangerous' permissions
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_CODE_FOR_LOCATION_PERMISSIONS);
        }
    }




    //Check if location permission requested in the Manifest has been granted
    private boolean hasLocationPermission(){

        //If permission is granted, result = PackageManager.PERMISSION_GRANTED (else, PackageManager.PERMISSON_DENIED).
        //NOTE: Permissions ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION are in the same PERMISSION GROUP, called ADDRESS.
        //If one permission is a permission group is granted/denied access, the same applies to all other permissions in that group.
        // Other groups includej: CALENDAR, CAMERA, CONTACTS, MICROPHONE, PHONE, SENSORS, SMS, STORAGE.
        int result = ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);

        //Return a boolean for state of location permission
        return (result == PackageManager.PERMISSION_GRANTED);
    }





    //Override onStart() fragment lifecycle callback method
    @Override
    public void onStart(){
        super.onStart();

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


        //======= Add first pix layout =========================================
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
            }
        });


        //Create/call the Adapter and link it with the RecyclerView
        updateUI();


        return view;
    }





    //Helper method for creating setting the list view, including setting "Add New Pix" button and set up Adapter and linking it with the RecyclerView
    public void updateUI(){

        //Assign the mPixes reference variable to the List of Pix objects from the PixManager singleton
        final List<Pix> mPixes = PixManager.get(getActivity()).getPixes();


        //============ Set visibility of "no pix view" - if no Pixes exist ==============================
        if (mPixes.size() == 0){
            mNoPixView.setVisibility(View.VISIBLE);
        }
        else{
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

        if (PixManager.get(getActivity()).getPixes().size() > 0) {
            //Inflate a menu hiearchy from specified resource
            menuInflater.inflate(R.menu.fragment_pix_list, menu);
        }
    }





    //Override onOptionsItemSelected(..) fragment lifecycle callback method
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        Log.i(TAG, "onOptionsItemsSelected(..) called");

        switch(menuItem.getItemId()){

            case(R.id.new_pix):

                //Create a Pix object
                Pix pix = new Pix();

                //Add the Pix object to the SQLiteDatabase, "pixes"
                PixManager.get(getActivity()).addPix(pix);

                //Create/call the Adapter and link it with the RecyclerView
                updateUI();

                //Open up the Pix (just created)
                mCallbacks.onPixSelected(pix);

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

            //Create LayoutInfater from the given Context
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            //Inflate the View
            View view = layoutInflater.inflate(R.layout.list_item_pix, viewGroup, false);

            //Pass the inflated View into the ViewHolder constructor
            return new PixViewHolder(view);
        }



        //Override method from the ReyclerView.Adapter inner class of Adapter
        @Override
        public void onBindViewHolder(PixViewHolder pixViewHolder, int position){

            //Get a specific Pix obect, 'pix', from the List of Pix objects, 'mPixes'
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







        //Build constructor #1
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

            //Assign list item's locatoin instance variable to its associated resource ID
            mPixAddress = (TextView) view.findViewById(R.id.list_pix_location);

            //Assign list item's tag instance variable to its associatred resource ID
            mPixTagged = (TextView) view.findViewById(R.id.list_pix_tagged);


            //Set listener for list item
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
                    if (mPictureFile.length() != 0) {

                        //Open picture view dialog
                        ImageViewFragment pictureViewDialog = ImageViewFragment.newInstance(mPictureFile);

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


            //If the two-pane layout is active (i.e. sw > 600dp)
            if (getActivity().findViewById(R.id.detail_fragment_container) != null) {

                //Update PixListFragment() in 'real-time' for two-pane layout
                mCallbacks.onPixUpdatedFromListView(mPix);
            }
        }



        //Stash the Pix object sent from the Adapter
        public void bind(Pix pix){

            //Assign the Pix instance variable to that sent from the Adapter
            mPix = pix;

            //Set the text of the list item's title
            if (mPix.getTitle() == null || mPix.getTitle().isEmpty()){
                mPixTitle.setText("* Untitled *");
                mPixTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.yellow));
                mPixTitle.setTypeface(null, Typeface.ITALIC);
            }
            else{
                mPixTitle.setText(mPix.getTitle());
                mPixTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.dark_gray));
            }

            //Set the text of the list item's description
            if (mPix.getDescription() == null || mPix.getDescription().isEmpty()){
                mPixDescription.setText("* No description *");
                mPixDescription.setTypeface(null, Typeface.ITALIC);
                mPixDescription.setTextColor(ContextCompat.getColor(getActivity(), R.color.dark_yellow));
            }
            else{
                mPixDescription.setText(mPix.getDescription());
            }

            //Set new date display for date button
            mPixDate.setText(mPixDateFormat.format("EEE d MMM yy", mPix.getDate()));

            //Set the text of the list item's location
            if (mPix.getAddress() == null){
                //Display nothing on location field
                mPixAddress.setText("");
            }
            else{
                //Display loality on location field (i.e. Footscray, Victoria)
                mPixAddress.setText("- in " + mPix.getLocality());
            }

            //Set the text of the list items tagged field
            if(mPix.getTag() == null || mPix.getTag().isEmpty()){
                mPixTagged.setText("");
            }
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
                pixTaggedString = "- with" + pixTaggedString;
                //Since there will be a "-" before the first contact, replace the "- with-" with "- with"
                pixTaggedString = pixTaggedString.replaceAll("- with-", "- with");
                //Replace the ", and" with "and" (effectively removing the comma before " and")
                pixTaggedString = pixTaggedString.replaceAll(", and", " and");


                //Display pixTaggedString in the mPixTagged Button
                mPixTagged.setText(pixTaggedString);
            }


            //If layout is in two-pane (i.e. sw > 600dp)
            if (getActivity().findViewById(R.id.detail_fragment_container) != null){
                //Remove text display of address
                mPixAddress.setText("");
                //Remove text display of tagged contacts
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

                //Scale picture to fit allocated ImageView space
                mPictureView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                //Talkback accessbility: Associate textual description to 'existing' view
                mPictureView.setContentDescription(getString(R.string.pix_picture_description));
            }
        }




    }






    //Define ItemDecoration class - a class that allows manipulation of list item interface
    //Purpose: To add divider (lines) to list items
    public class PixItemDecoration extends RecyclerView.ItemDecoration {

        //Declare Drawable interface for dividers
        private Drawable mDivider;

        //Build constructor
        public PixItemDecoration(Context context) {
            //Get Drawable instance variable from Context
            mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider);
        }

        //Override callback method - Draw decorations into the Canvas supplied to the RecyclerView.
        // Drawing will occur AFTER the item views are drawn and will thus appear OVER the views.
        @Override
        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            //Get LEFT horizontal co-ordinate of list item
            int leftCoordinate = recyclerView.getPaddingLeft();

            //Get RIGHT horizontal co-ordinate of list item
            int rightCoordinate = recyclerView.getWidth() - recyclerView.getPaddingRight();

            //Get number of View children in the RecyclerView. Views might be: Bitmap, TextView, etc.
            int viewChildrenOfListItemCount = recyclerView.getChildCount();

            //Cycle over all View children in the RecyclerView
            for (int i = 0; i < viewChildrenOfListItemCount; i++) {

                //Get the View
                View viewChildOfListItem = recyclerView.getChildAt(i);

                //Get the LayoutParams associated with this view. All views should have layout parameters.
                // Layout parameters supply parameters to the parent of this view specifying how it should be arranged.
                RecyclerView.LayoutParams listItemLayoutParams = (RecyclerView.LayoutParams) viewChildOfListItem.getLayoutParams();

                //Get TOP co-ordinate of list item
                int topCoordinate = viewChildOfListItem.getBottom() + listItemLayoutParams.bottomMargin;

                //Get BOTTOM co-ordinate of list item
                int bottomCoordinate = topCoordinate + mDivider.getIntrinsicHeight();

                //Set bounds for where the divider (lines) are to appaear
                mDivider.setBounds(leftCoordinate, topCoordinate, rightCoordinate, bottomCoordinate);

                //Draw the divider (lines) on set bounds
                mDivider.draw(canvas);
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
