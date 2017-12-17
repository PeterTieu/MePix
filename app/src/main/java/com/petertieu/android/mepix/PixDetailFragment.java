package com.petertieu.android.mepix;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import android.text.format.DateFormat;
import java.util.UUID;

/**
 * Created by Peter Tieu on 14/12/2017.
 */



//Fragment for the detail view
public class PixDetailFragment extends Fragment {

    //Define 'key' for the argument-bundle
    private static final String ARGUMENT_PIX_ID = "pix_id";

    //Define TAG for Logcat
    private static final String TAG = "PixDetailFragment";

    //Declare Pix instance variable
    private Pix mPix;

    //Declare View instance variables
    private EditText mTitle;    //Title
    private Button mDateButton; //Date Button



    private DateFormat mCurrentDate;
    private static final int REQUEST_DATE = 0;
    private static final String DIALOG_DATE = "DialogDate";






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

        //Callback method for when a Pix's instance variable is changed... inside the detail view
        void onPixUpdated(Pix pix);
    }





    //Override onAttach() fragment lifecycle callback method
    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        //Log in Logcat
        Log.i(TAG, "onAttach() called");
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



        //================ SET UP mDate ==================================================================
        //Assign date button instance variable to its associated resource ID
        mDateButton = (Button) view.findViewById(R.id.detail_pix_date);

        //If a date exists for the Pix
        if (mPix.getDate() != null){

            //Set text of the date button to date of the Pix
            mDateButton.setText(mCurrentDate.format("EEE d MMM yyyy", mPix.getDate()));
        }


        //Set listener the Date button
        mDateButton.setOnClickListener(new View.OnClickListener(){

            //Override onClick(..) from View.OnClickListener
            @Override
            public void onClick(View view){

                FragmentManager fragmentManager = getFragmentManager();

                DatePickerFragment datePickerDialog = DatePickerFragment.newInstance(mPix.getDate());


                datePickerDialog.setTargetFragment(PixDetailFragment.this, REQUEST_DATE);

                datePickerDialog.show(fragmentManager, DIALOG_DATE);


            }
        });













        //Return the View
        return view;
    }




    //Update the Pix (upon any changes)
    private void updatePix(){

        //Update the SQLite database based on the Pix passed
        PixManager.get(getActivity()).updatePixOnDatabase(mPix);

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
    }
}
