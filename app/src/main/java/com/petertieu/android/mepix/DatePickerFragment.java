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

/**
 * Created by Peter Tieu on 17/12/2017.
 */

public class DatePickerFragment extends DialogFragment {

    private static final String ARG_PIX_DATE = "pixDate";

    private static final String EXTRA_DATE = "com.petertieu.android.mepix";

    DatePicker mDatePicker;



    //Build encapsulating 'constructor'
    public static DatePickerFragment newInstance(Date pixDate){

        //Create Bundle object (i.e. argument-bundle)
        Bundle argumentBundle = new Bundle();

        //Set key-value pairs for the argument bundle
        argumentBundle.putSerializable(ARG_PIX_DATE, pixDate);

        //Create DatePickerFragment
        DatePickerFragment datePickerFragment = new DatePickerFragment();

        //Set argument-bundle for the PixDetailFragment
        datePickerFragment.setArguments(argumentBundle);

        //return DatePickerFragment
        return datePickerFragment;
    }





    //
    public Dialog onCreateDialog(Bundle savedInstanceState){

        final Date pixDate = (Date) getArguments().getSerializable(ARG_PIX_DATE);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_date_picker, null);

        mDatePicker = (DatePicker) view.findViewById(R.id.dialog_pix_date_picker);


        Calendar calendar = Calendar.getInstance();

        calendar.setTime(pixDate);

        calendar.setTime(pixDate);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DATE);


        mDatePicker.init(year, month, dayOfMonth, null);

        return new AlertDialog
                .Builder(getActivity())
                .setView(view)
                .setTitle(R.string.date_picker_title)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int year = mDatePicker.getYear();
                                int month = mDatePicker.getMonth();
                                int dayOfMonth = mDatePicker.getDayOfMonth();
                                sendResult(Activity.RESULT_OK, pixDate);
                            }
                        })
                .create();

    }





    private void sendResult(int resultCode, Date date){

        if (getTargetFragment() == null){
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);


        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }







}
