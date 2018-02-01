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




//Dialog fragment to confirm 'Pix Location Update'.
//Called by a menu item in the toolbar of PixDetailFragment fragment
public class PixUpdateLocationConfirmationDialogFragment extends DialogFragment{


    //============ Declare instance variables ===================================
    //Define key for argument-bundle to store STORED Pix address line
    public static final String ARG_PIX_STORED_ADDRESS_LINE = "pixStoredAddressLine";

    //Define key for argument-bundle to store CURRENT Pix address line (i.e. the actual current location of the device)
    public static final String ARG_PIX_CURRENT_ADDRESS_LINE = "pixCurrentAddressLine";

    //Define key identifier for passing Pix location update confirmation (boolean value)
    public static final String EXTRA_PIX_UPDATE_LOCATION_CONFIRMATION = "pixLocationUpdateConfirmation";

    //Declare AlertDialogLayout view (i.e. view that diaplays dialog)
    public AlertDialogLayout mAlertDialogLayout;





    //============ Decfine methods ===================================

    //Build encapsulating 'constructor'
    public static PixUpdateLocationConfirmationDialogFragment newInstance(String storedAddressLine, String currentAddressLine){

        //Create argument-bundle (Bundle object)
        Bundle argumentBundle = new Bundle();

        //Store key-value pairs in argument-bundle
        argumentBundle.putString(ARG_PIX_STORED_ADDRESS_LINE, storedAddressLine); //Pix STORED address line
        argumentBundle.putString(ARG_PIX_CURRENT_ADDRESS_LINE, currentAddressLine); //Pix CURRENT address line

        //Create object of this class
        PixUpdateLocationConfirmationDialogFragment pixUpdateLocationConfirmationDialogFragment = new PixUpdateLocationConfirmationDialogFragment();

        //Set argument-bundle to this object
        pixUpdateLocationConfirmationDialogFragment.setArguments(argumentBundle);

        //Return object of this class, which has argument-bundle
        return pixUpdateLocationConfirmationDialogFragment;
    }





    //Override onCreateDialog(..) callback method - to set up dialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        //Get Pix STORED addres line value from argument-bundle
        String pixStoredAddressLine = (String) getArguments().getString(ARG_PIX_STORED_ADDRESS_LINE);

        //Get Pix CURRENT addres line value from argument-bundle
        String pixCurrentAddressLine = (String) getArguments().getString(ARG_PIX_CURRENT_ADDRESS_LINE);

        //Set-up custom title to display in the dialog
        TextView dialogTitle = new TextView(getActivity()); //Create TextView object
        dialogTitle.setText("\nUpdate Location of this Pix\n"); //Set text on TextView
        dialogTitle.setTextSize(22); //Set size of text
        dialogTitle.setGravity(Gravity.CENTER); //Set  position of text in the title box of the dialog
        dialogTitle.setTypeface(null, Typeface.BOLD); //Set the text to be bold//Set the text to be bold
        dialogTitle.setTextColor(getResources().getColor(R.color.colorButton)); //Set text color
        dialogTitle.setBackgroundColor(getResources().getColor(R.color.yellow)); //Set text background color


        //Create View object for dialog
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_update_location, null);

        //Assign AlertDialogLayout reference variable to associated resource ID in layout file
        mAlertDialogLayout = (AlertDialogLayout) view.findViewById(R.id.dialog_pix_update_location);

        //If the STORED address line is the SAME as the CURRENT address line
        if (pixStoredAddressLine.equals(pixCurrentAddressLine) || pixStoredAddressLine == pixCurrentAddressLine){
            //Build the AlertDialog
            return new AlertDialog
                    .Builder(getActivity())
                    .setView(view) //Set the custom view to be the conents of the AlertDialog
                    .setCustomTitle(dialogTitle) //Set title of dialog to the TextView object (above)
                    .setMessage(Html.fromHtml( //Set message of dialog
                            "<br>" + "</br>" + //New line
                                    "<b>" + "Pix Location matches your Current Location:" + "</b>" + //Information line (bold)
                                    "<br>" + "</br>" + //New line
                                    " " + pixStoredAddressLine +
                                    "<br>" + "</br>" + //New line
                                    "<br>" + "</br>" + //New line
                                    "<br>" + "</br>" + //New line
                                    "<br>" + "</br>" + //New line
                                    "<b>" + "Updating Pix Location will not result in any changes" + "</b>")) //Prompt line (bold)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }
        //If the STORED address line is NOT the same as the CURRENT address line
        else{
            //Build the AlertDialog
            return new AlertDialog
                    .Builder(getActivity())
                    .setView(view) //Set the custom view to be the conents of the AlertDialog
                    .setCustomTitle(dialogTitle) //Set title of dialog to the TextView object (above)
                    .setMessage(Html.fromHtml( //Set message of dialog
                            "<br>" + "</br>" + //New line
                                    "<br>" + "</br>" + //New line
                                    "<b>" + "Pix Location:" + "</b>" + " " + pixStoredAddressLine + //STORED address line line (bolded)
                                    "<br>" + "</br>" + //New line
                                    "<br>" + "</br>" + //New line
                                    "<b>" + "Your Current Location:" + "</b>" + " " + pixCurrentAddressLine + //CURRENT address line line (bolded)
                                    "<br>" + "</br>" + //New line
                                    "<br>" + "</br>" + //New line
                                    "<br>" + "</br>" + //New line
                                    "<br>" + "</br>" + //New line
                                    "<b>" + "Do you wish to update Pix Location to your Current Location?" + "</b>")) //Prompt line (bolded)
                    .setNegativeButton(android.R.string.cancel, null) //Set negative button
                    .setPositiveButton(R.string.confirm_update_location_button, //Set positive button
                            //Set listener for positive button
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Create boolean to indicate positive button is pressed
                                    boolean confirmUpdateLocation = true;

                                    //Sent results to target fragment (PixDetailFragment)
                                    sendResult(Activity.RESULT_OK, confirmUpdateLocation);
                                }
                            })
                    .create(); //Create the dialog
        }
    }





    //Send boolean result for positive button press back to target fragment (PixDetailFragment)
    private void sendResult(int resultCode, boolean confirmUpdateLocation){

        //If target fragment (PixDetailFragment) does NOT exist
        if (getTargetFragment() == null){
            return;
        }

        //Create Intent object
        Intent intent = new Intent();

        //Add key-value pair to the Intent (for positive button press)
        intent.putExtra(EXTRA_PIX_UPDATE_LOCATION_CONFIRMATION, confirmUpdateLocation);

        //Call onActivityResult(..) of target fragment (PixDetailFragment)
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

}
