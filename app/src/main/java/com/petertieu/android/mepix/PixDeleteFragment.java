package com.petertieu.android.mepix;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AlertDialogLayout;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Peter Tieu on 22/12/2017.
 */

public class PixDeleteFragment extends DialogFragment{

    private static final String ARG_PIX_TITLE = "pixTitle";

    AlertDialogLayout mAlertDialogLayout;



    public static PixDeleteFragment newInstance(String pixTitle){

        Bundle argumentBundle = new Bundle();

        argumentBundle.putString(ARG_PIX_TITLE, pixTitle);

        PixDeleteFragment pixDeleteFragment = new PixDeleteFragment();

        pixDeleteFragment.setArguments(argumentBundle);

        return pixDeleteFragment;
    }




    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String pixTitle = (String) getArguments().getString(ARG_PIX_TITLE);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_delete, null);

        mAlertDialogLayout = (AlertDialogLayout) view.findViewById(R.id.dialog_pix_delete);

        return new AlertDialog
                .Builder(getActivity())
                .setView(view)
                .setTitle("Are you sure you want to delete Pix:\n\n" + pixTitle)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                boolean confirmDelete = true;

                                sendResult(Activity.RESULT_OK, confirmDelete);
                            }
                }).create();
    }





    private void sendResult(int resultCode, boolean confirmDelete){

        if (getTargetFragment() == null){
            return;
        }


        Intent intent = new Intent();

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);

    }





}



