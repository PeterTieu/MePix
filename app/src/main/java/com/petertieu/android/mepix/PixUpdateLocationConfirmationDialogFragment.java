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

/**
 * Created by Peter Tieu on 25/01/2018.
 */

public class PixUpdateLocationConfirmationDialogFragment extends DialogFragment{


    public static final String ARG_PIX_STORED_ADDRESS_LINE = "pixStoredAddressLine";
    public static final String ARG_PIX_CURRENT_ADDRESS_LINE = "pixCurrentAddressLine";

    public AlertDialogLayout mAlertDialogLayout;

    public static final String EXTRA_PIX_UPDATE_LOCATION_CONFIRMATION = "pixLocationUpdateConfirmation";



    public static PixUpdateLocationConfirmationDialogFragment newInstance(String storedAddressLine, String currentAddressLine){

        Bundle argumentBundle = new Bundle();

        argumentBundle.putString(ARG_PIX_STORED_ADDRESS_LINE, storedAddressLine);

        argumentBundle.putString(ARG_PIX_CURRENT_ADDRESS_LINE, currentAddressLine);


        PixUpdateLocationConfirmationDialogFragment pixUpdateLocationConfirmationDialogFragment = new PixUpdateLocationConfirmationDialogFragment();

        pixUpdateLocationConfirmationDialogFragment.setArguments(argumentBundle);

        return pixUpdateLocationConfirmationDialogFragment;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        String pixStoredAddressLine = (String) getArguments().getString(ARG_PIX_STORED_ADDRESS_LINE);
        String pixCurrentAddressLine = (String) getArguments().getString(ARG_PIX_CURRENT_ADDRESS_LINE);


        TextView dialogTitle = new TextView(getActivity());
        dialogTitle.setText("\nUpdate Location of this Pix\n");
        dialogTitle.setTextSize(22);
        dialogTitle.setGravity(Gravity.CENTER);
        dialogTitle.setTypeface(null, Typeface.BOLD);
        dialogTitle.setTextColor(getResources().getColor(R.color.colorButton));
        dialogTitle.setBackgroundColor(getResources().getColor(R.color.yellow));


        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_update_location, null);

        mAlertDialogLayout = (AlertDialogLayout) view.findViewById(R.id.dialog_pix_update_location);



        if (pixStoredAddressLine.equals(pixCurrentAddressLine) || pixStoredAddressLine == pixCurrentAddressLine){
            return new AlertDialog
                    .Builder(getActivity())
                    .setView(view)
                    .setCustomTitle(dialogTitle)
                    .setMessage(Html.fromHtml(
                            "<br>" + "</br>" +
                                    "<b>" + "Pix Location matches your Current Location:" + "</b>" +
                                    "<br>" + "</br>" +
                                    " " + pixStoredAddressLine +
                                    "<br>" + "</br>" +
                                    "<br>" + "</br>" +
                                    "<br>" + "</br>" +
                                    "<br>" + "</br>" +
                                    "<b>" + "Updating Pix Location will not result in any changes" + "</b>"))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        else{

            return new AlertDialog
                    .Builder(getActivity())
                    .setView(view)
                    .setCustomTitle(dialogTitle)
                    .setMessage(Html.fromHtml(
                            "<br>" + "</br>" +
                                    "<br>" + "</br>" +
                                    "<b>" + "Pix Location:" + "</b>" + " " + pixStoredAddressLine +
                                    "<br>" + "</br>" +
                                    "<br>" + "</br>" +
                                    "<b>" + "Your Current Location:" + "</b>" + " " + pixCurrentAddressLine +
                                    "<br>" + "</br>" +
                                    "<br>" + "</br>" +
                                    "<br>" + "</br>" +
                                    "<br>" + "</br>" +
                                    "<b>" + "Do you wish to update Pix Location to your Current Location?" + "</b>"))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.confirm_update_location_button,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    boolean confirmUpdateLocation = true;

                                    sendResult(Activity.RESULT_OK, confirmUpdateLocation);
                                }
                            })
                    .create();
        }



    }


    private void sendResult(int resultCode, boolean confirmUpdateLocation){

        if (getTargetFragment() == null){
            return;
        }

        Intent intent = new Intent();

        intent.putExtra(EXTRA_PIX_UPDATE_LOCATION_CONFIRMATION, confirmUpdateLocation);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

}
