package com.petertieu.android.mepix;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;




//DialogFragment for DatePicker dialog
public class DateDialogFragment extends DialogFragment {

    //Declare 'key'
    private static final String ARG_PIX_DATE = "pixDate";

    //Define identifier for dialog fragment extra
    public static final String EXTRA_DATE = "com.petertieu.android.mepix";

    //Declare layout View of the dialog
    DatePicker mDatePicker;



    //Build encapsulating 'constructor'
    public static DateDialogFragment newInstance(Date pixDate){

        //Create Bundle object (i.e. argument-bundle)
        Bundle argumentBundle = new Bundle();

        //Set key-value pairs for argument-bundle
        argumentBundle.putSerializable(ARG_PIX_DATE, pixDate);

        //Create DateDialogFragment
        DateDialogFragment dateDialogFragment = new DateDialogFragment();

        //Set argument-bundle for the PixDetailFragment
        dateDialogFragment.setArguments(argumentBundle);

        //Return DateDialogFragment object
        return dateDialogFragment;
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

        //Create Calendar object, which could take Date objects and convert them into constituent properties (e.g. year, month, day of month, hour, etc.)
        Calendar calendar = Calendar.getInstance();

        //Set time in Calendar to time stored in the Pix object
        calendar.setTime(pixDate);

        //Get year/month/dayOfMonth from Calendar - i.e. saved from the Pix
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DATE);

        //Initialise DatePicker object
        mDatePicker.init(year, month, dayOfMonth, null);


        TextView dialogTitle = new TextView(getActivity());
        dialogTitle.setText(R.string.date_picker_title);
        dialogTitle.setTextColor(getResources().getColor(R.color.colorButton));
        dialogTitle.setTextSize(25);
        dialogTitle.setTypeface(null, Typeface.BOLD);

        //Return AlertDialog (subclass of Dialog), which sets the dialog properties
        return new AlertDialog
                .Builder(getActivity()) //Create Builder
                .setView(view) //Set View of dialog
                .setCustomTitle(dialogTitle) //Set TITLE of dialog
                .setNeutralButton(android.R.string.cancel, null) //Set NEGATIVE BUTTON of the dialog. null: no listener for the cancel button
                .setNegativeButton(R.string.set_date_to_today, new DialogInterface.OnClickListener() { //Set NEUTRAL BUTTON of the dialog, and a listener to set Date to CURRENT date
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendResult(Activity.RESULT_OK, new Date());
                    }
                })
                .setPositiveButton(R.string.set_date_to_selected, //Set POSITIVE BUTTON of the dialog, and a listener for it
                        new DialogInterface.OnClickListener() {

                            //Override listener of DialogInterface.OnClickListener.OnClickListener interface
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int year = mDatePicker.getYear(); //Get 'year' selected from DatePicker view
                                int month = mDatePicker.getMonth(); //Get 'month' selected from DatePicker view
                                int dayOfMonth = mDatePicker.getDayOfMonth(); //Get 'dayOfMonth' selected from DatePicker view

                                //Save set year/month/dayOfMonth to Date object
                                Date newSetDate = new GregorianCalendar(year, month, dayOfMonth).getTime();

                                //Send new Date data back to hosting activity (PixViewPagerActivity)
                                sendResult(Activity.RESULT_OK, newSetDate);
                            }
                        })
                .create();

    }





    //Send result to the hosting activity
    private void sendResult(int resultCode, Date newSetDate){

        //If hosting fragment (PixDetailFragment) DOES NOT exist
        if (getTargetFragment() == null){
            return;
        }

        //Create Intent
        Intent intent = new Intent();

        //Add Date data as 'extra'
        intent.putExtra(EXTRA_DATE, newSetDate);

        //Send resultCode and Intent to hosting fragment (PixDetailFragment)
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
