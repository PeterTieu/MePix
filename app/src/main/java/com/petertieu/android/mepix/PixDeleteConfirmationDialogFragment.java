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


    //============ Declare instance variables ===================================
    //Define key for argument-bundle to store Pix title
    private static final String ARG_PIX_TITLE = "pixTitle";

    //Define key for argument-bundle to store Pix description
    public static final String ARG_PIX_DESCRIPTION = "pixDescription";

    //Define key identifier for passing Pix delete confirmation (boolean value)
    public static final String EXTRA_PIX_DELETE_CONFIRMATION = "pixDeleteConfirmation";

    //Declare AlertDialogLayout view (i.e. view that diaplays dialog)
    AlertDialogLayout mAlertDialogLayout;





    //============ Decfine methods ===================================

    //Build encapsulating 'constructor'
    public static PixDeleteConfirmationDialogFragment newInstance(String pixTitle, String pixDescription){

        //Create argument-bundle (Bundle object)
        Bundle argumentBundle = new Bundle();

        //Store key-value pairs in argument-bundle
        argumentBundle.putString(ARG_PIX_TITLE, pixTitle);  //Pix title
        argumentBundle.putString(ARG_PIX_DESCRIPTION, pixDescription); //Pix description

        //Create object of this class
        PixDeleteConfirmationDialogFragment pixDeleteConfirmationDialogFragment = new PixDeleteConfirmationDialogFragment();

        //Set argument-bundle to this object
        pixDeleteConfirmationDialogFragment.setArguments(argumentBundle);

        //Return object of this class, which has argument-bundle
        return pixDeleteConfirmationDialogFragment;
    }





    //Override onCreateDialog(..) callback method - to set up dialog
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Get Pix title value from argument-bundle
        String pixTitle = (String) getArguments().getString(ARG_PIX_TITLE);

        //Get Pix description value from argument-bundle
        String pixDescription = (String) getArguments().getString(ARG_PIX_DESCRIPTION);

        //Set-up custom title to display in the dialog
        TextView dialogTitle = new TextView(getActivity()); //Create TextView object
        dialogTitle.setText("\nDelete this Pix\n"); //Set text on TextView
        dialogTitle.setTextSize(22); //Set size of text
        dialogTitle.setGravity(Gravity.CENTER); //Set  position of text in the title box of the dialog
        dialogTitle.setTypeface(null, Typeface.BOLD); //Set the text to be bold
        dialogTitle.setTextColor(getResources().getColor(R.color.colorButton)); //Set text color
        dialogTitle.setBackgroundColor(getResources().getColor(R.color.yellow)); //Set text background color


        //If Pix title does NOT exist, is empty, or contains only spaces
        if (pixTitle == null || pixTitle.isEmpty() || pixTitle.trim().length()==0){
            pixTitle = "<i>" + "*Untitled*"  + "</i>";
        }

        //If Pix description does NOT exist, is empty, or contains only spaces
        if (pixDescription == null || pixDescription.isEmpty() || pixDescription.trim().length()==0){
            pixDescription = "<i>" + "*No description*"  + "</i>";
        }


        //Create View object for dialog
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_delete, null);

        //Assign AlertDialogLayout reference variable to associated resource ID in layout file
        mAlertDialogLayout = (AlertDialogLayout) view.findViewById(R.id.dialog_pix_delete);

        //Build the AlertDialog
        return new AlertDialog
                .Builder(getActivity())
                .setView(view)  //Set the custom view to be the conents of the AlertDialog
                .setCustomTitle(dialogTitle) //Set title of dialog to the TextView object (above)
                .setMessage(Html.fromHtml( //Set message of dialog
                        "<br>" + "</br>" + //New line
                        "<br>" + "</br>" + //New line
                        "<b>" + "Title:" + "</b>" + " " + pixTitle + //Title line (where "Title:" is bolded)
                        "<br>" + "</br>" + //New line
                        "<br>" + "</br>" + //New line
                        "<b>" + "Description:" + "</b>" + " " + pixDescription + //Description line (where "Description:" is bolded)
                        "<br>" + "</br>" + //New line
                        "<br>" + "</br>" + //New line
                        "<br>" + "</br>" + //New line
                        "<br>" + "</br>" + //New line
                        "<b>" + "Do you wish to delete this Pix?" + "</b>")) //Prompt line (bolded)
                .setNegativeButton(android.R.string.cancel, null) //Set negative button
                .setPositiveButton(R.string.confirm_delete_button, //Set positive button
                        //Set listener for positive button
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Create boolean to indicate positive button is pressed
                                boolean confirmDelete = true;

                                //Send result to the target fragment (PixDetailFragment)
                                sendResult(Activity.RESULT_OK, confirmDelete);
                            }
                })
                .create(); //Create the dialog
    }





    //Send boolean result for positive button press back to target fragment (PixDetailFragment)
    private void sendResult(int resultCode, boolean confirmDelete){

        //If target fragment (PixDetailFragment) does NOT exist
        if (getTargetFragment() == null){
            return;
        }

        //Create Intent object
        Intent intent = new Intent();

        //Add key-value pair to the Intent (for positive button press)
        intent.putExtra(EXTRA_PIX_DELETE_CONFIRMATION, confirmDelete);

        //Call onActivityResult(..) of target fragment (PixDetailFragment)
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);

    }





}



