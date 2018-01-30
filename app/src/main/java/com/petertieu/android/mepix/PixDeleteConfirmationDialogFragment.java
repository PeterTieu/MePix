package com.petertieu.android.mepix;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AlertDialogLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;




//Dialog fragment to confirm 'Pix Delete'.
//Called by a menu item in the toolbar of PixDetailFragment fragment
public class PixDeleteConfirmationDialogFragment extends DialogFragment{

    //Declare instance variables
    private static final String ARG_PIX_TITLE = "pixTitle";
    public static final String ARG_PIX_DESCRIPTION = "pixDescription";

    //Define identifier for dialog fragment extra
    public static final String EXTRA_PIX_DELETE_CONFIRMATION = "pixDeleteConfirmation";


    AlertDialogLayout mAlertDialogLayout;



    public static PixDeleteConfirmationDialogFragment newInstance(String pixTitle, String pixDescription){

        Bundle argumentBundle = new Bundle();

        argumentBundle.putString(ARG_PIX_TITLE, pixTitle);
        argumentBundle.putString(ARG_PIX_DESCRIPTION, pixDescription);

        PixDeleteConfirmationDialogFragment pixDeleteConfirmationDialogFragment = new PixDeleteConfirmationDialogFragment();

        pixDeleteConfirmationDialogFragment.setArguments(argumentBundle);

        return pixDeleteConfirmationDialogFragment;
    }




    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String pixTitle = (String) getArguments().getString(ARG_PIX_TITLE);
        String pixDescription = (String) getArguments().getString(ARG_PIX_DESCRIPTION);

        TextView dialogTitle = new TextView(getActivity());
        dialogTitle.setText("\nDelete this Pix\n");
        dialogTitle.setTextSize(22);
        dialogTitle.setGravity(Gravity.CENTER);
        dialogTitle.setTypeface(null, Typeface.BOLD);
        dialogTitle.setTextColor(getResources().getColor(R.color.colorButton));
        dialogTitle.setBackgroundColor(getResources().getColor(R.color.yellow));


        if (pixTitle == null || pixTitle.isEmpty() || pixTitle.trim().length()==0){
            pixTitle = "<i>" + "*Untitled*"  + "</i>";
        }
        if (pixDescription == null || pixDescription.isEmpty() || pixDescription.trim().length()==0){
            pixDescription = "<i>" + "*No description*"  + "</i>";
        }

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_delete, null);

        mAlertDialogLayout = (AlertDialogLayout) view.findViewById(R.id.dialog_pix_delete);

        return new AlertDialog
                .Builder(getActivity())
                .setView(view)
                .setCustomTitle(dialogTitle)
                .setMessage(Html.fromHtml(
                        "<br>" + "</br>" +
                        "<br>" + "</br>" +
                        "<b>" + "Title:" + "</b>" + " " + pixTitle +
                        "<br>" + "</br>" +
                        "<br>" + "</br>" +
                        "<b>" + "Description:" + "</b>" + " " + pixDescription +
                        "<br>" + "</br>" +
                        "<br>" + "</br>" +
                        "<br>" + "</br>" +
                        "<br>" + "</br>" +
                        "<b>" + "Do you wish to delete this Pix?" + "</b>"))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.confirm_delete_button,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                boolean confirmDelete = true;

                                sendResult(Activity.RESULT_OK, confirmDelete);
                            }
                })
                .create();
    }





    private void sendResult(int resultCode, boolean confirmDelete){

        if (getTargetFragment() == null){
            return;
        }


        Intent intent = new Intent();

        intent.putExtra(EXTRA_PIX_DELETE_CONFIRMATION, confirmDelete);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);

    }





}



