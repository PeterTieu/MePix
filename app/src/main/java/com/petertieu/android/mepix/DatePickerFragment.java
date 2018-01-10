package com.petertieu.android.mepix;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Peter Tieu on 17/12/2017.
 */


//DialogFragment for DatePicker dialog
public class DatePickerFragment extends DialogFragment {

    //Declare 'key'
    private static final String ARG_PIX_DATE = "pixDate";

    //Define identifier for dialog fragment extra
    public static final String EXTRA_DATE = "com.petertieu.android.mepix";

    //Declare layout View of the dialog
    DatePicker mDatePicker;



    //Build encapsulating 'constructor'
    public static DatePickerFragment newInstance(Date pixDate){

        //Create Bundle object (i.e. argument-bundle)
        Bundle argumentBundle = new Bundle();

        //Set key-value pairs for argument-bundle
        argumentBundle.putSerializable(ARG_PIX_DATE, pixDate);

        //Create DatePickerFragment
        DatePickerFragment datePickerFragment = new DatePickerFragment();

        //Set argument-bundle for the PixDetailFragment
        datePickerFragment.setArguments(argumentBundle);

        //Return DatePickerFragment object
        return datePickerFragment;
    }





    //Override lifecycle callback method from DialogFragment
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        //Get 'value' from argument-bundle
        final Date pixDate = (Date) getArguments().getSerializable(ARG_PIX_DATE);

        //Inflate DatePicker dialog layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_date_picker, null);

        //Assign DatePicker reference variable to associated resource ID
        mDatePicker = (DatePicker) view.findViewById(R.id.dialog_pix_date_picker);

        //Create Calendar object
        Calendar calendar = Calendar.getInstance();

        //Set time in Calendar to time stored in the Pix object
        calendar.setTime(pixDate);

        //Get year/month/dayOfMonth from Calendar - i.e. saved from the Pix
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DATE);

        //Initialise DatePicker object
        mDatePicker.init(year, month, dayOfMonth, null);

        //Return AlertDialog (subclass of Dialog), which sets the dialog properties
        return new AlertDialog
                .Builder(getActivity()) //Create Builder
                .setView(view) //Set View of dialog
                .setTitle(R.string.date_picker_title) //Set TITLE of dialog
                .setNegativeButton(android.R.string.cancel, null) //Set NEGATIVE BUTTON of the dialog. null: no listener for the cancel button
                .setPositiveButton(android.R.string.ok, //Set POSITIVE BUTTON of the dialog, and a listener for it
                        new DialogInterface.OnClickListener() {

                            //Override listener of DialogInterface.OnClickListener.OnClickListener interface
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int year = mDatePicker.getYear(); //Get 'year' from DatePicker view
                                int month = mDatePicker.getMonth(); //Get 'month' from DatePicker view
                                int dayOfMonth = mDatePicker.getDayOfMonth(); //Get 'dayOfMonth' from DatePicker view

                                //Save set year/month/dayOfMonth to Date object
                                Date newSetDate = new GregorianCalendar(year, month, dayOfMonth).getTime();

                                //Send new Date data back to hosting activity (PixViewPagerActivity)
                                sendResult(Activity.RESULT_OK, newSetDate);
                            }
                        })
                .create();

    }





    //Send result to the hosting activity
    private void sendResult(int resultCode, Date newSetdate){

        //If hosting fragment (PixDetailFragment) DOES NOT exist
        if (getTargetFragment() == null){
            return;
        }

        //Create Intent
        Intent intent = new Intent();

        //Add Date data as 'extra'
        intent.putExtra(EXTRA_DATE, newSetdate);

        //Send resultCode and Intent to hosting fragment (PixDetailFragment)
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
