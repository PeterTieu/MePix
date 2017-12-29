package com.petertieu.android.mepix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;


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

    private File mPictureFile;

    private ImageView mPictureView;





    //Declare callbacks interface
    interface Callbacks{

        //Calback method for when a Pix is selected.. by either: New Pix created OR Pix selected in the list view
        void onPixSelected(Pix pix);
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
        List<Pix> mPixes = PixManager.get(getActivity()).getPixes();


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




            //Set a listener for the list item
            view.setOnClickListener(new View.OnClickListener(){

                //Override method of the View.OnClickListener interface of View
                @Override
                public void onClick(View view){

                    //Open up the Pix (existing)
                    mCallbacks.onPixSelected(mPix);
                }
            });
        }



        //Stash the Pix object sent from the Adapter
        public void bind(Pix pix){

            //Assign the Pix instance variable to that sent from the Adapter
            mPix = pix;

            //Set the text of the list item's title
            if (mPix.getTitle() == null || mPix.getTitle().isEmpty()){
                mPixTitle.setText("* Untitled *");
                mPixTitle.setTextSize(15f);
                mPixTitle.setTypeface(null, Typeface.ITALIC);
            }
            else{
                mPixTitle.setText(mPix.getTitle());
            }

            //Set the text of the list item's description
            if (mPix.getDescription() == null || mPix.getDescription().isEmpty()){
                mPixDescription.setText("* No description *");
                mPixDescription.setTextSize(10f);
                mPixDescription.setTypeface(null, Typeface.ITALIC);
            }
            else{
                mPixDescription.setText(mPix.getDescription());
            }

            //Set new date display for date button
            mPixDate.setText(mPixDateFormat.format("EEE d MMM yy", mPix.getDate()));


            //============ Set Favorited 'star' view===================================
            //Resource ID for favorited drawable ('yellow' star)
            int favoritedDrawable = getResources().getIdentifier("android:drawable/btn_star_big_on", null, null);

            //Resource ID for non-faovirted drawable ('grey' star)
            int nonFavoritedDrawable = getResources().getIdentifier("android:drawable/btn_star_big_off", null, null);

            //If: mPix.isFavorited() == true.. then: mPixFavorited.setImageResource(favoriteDrawable).. else: mPixFavorited.setImageResource(nonFavoriteDrawable)
            mPixFavorited.setImageResource(mPix.isFavorited() ? favoritedDrawable : nonFavoritedDrawable);


            //============ Set Picture view===================================
            if (mPix.getPictureFilename() != null){

                mPictureFile = PixManager.get(getActivity()).getPictureFile(mPix);
                updatePictureView();
            }

        }


        private void updatePictureView(){

            if (mPictureFile == null || !mPictureFile.exists()){
                mPictureView.setImageDrawable(null);

                mPictureView.setContentDescription(getString(R.string.pix_no_picture_description));
            }

            else{

                Bitmap bitmap = PictureUtility.getScaledBitmap(mPictureFile.getPath(), getActivity());

                mPictureView.setImageBitmap(bitmap);

                mPictureView.setContentDescription(getString(R.string.pix_picture_description));

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
