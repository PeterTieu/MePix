package com.petertieu.android.mepix;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;




//DialogFragment for ImageView dialog
public class PictureViewDialogFragment extends DialogFragment{


    //=================== Declare INSTANCE VARIABLES ===========================

    //Declare 'key' for argument bundle
    private static final String ARGUMENT_PICTURE = "image"; //Key for picture File
    private static final String ARGUMENT_TITLE = "title";   //Key for Pix title
    private static final String ARGUMENT_DATE = "date";     //Key for Pix date


    //Rotated version of the Bitmap generated from PictureUtility (in the correct orientation)
    private Bitmap bitmapPictureCorrectOrientation;


    private File mPictureFile;  //Pix File (to display in ImageView)
    private String mPixTitle;   //Pix title (to display in the filename of the picture file saved to Gallery)
    private Date mPixDate;      //Pix date ((to display in the filename of the picture file saved to Gallery)


    //Declare 'extra' indentifiers (for sending data to the target fragment (i.e. PixDetailFragment))
    public static final String EXTRA_PIX_PICTURE_SAVED_TO_GALLERY = "pictureSaved";         //Identifier key to indicate whether the "Save Picture to Gallery" button was pressed
    public static final String EXTRA_PIX_PICTURE_DELETE_SELECTED = "pictureDelteSelected";  //Indeifier key to indicate whether the "Delete Picture" button was pressed


    //Declare flag (for sending to the target fragment (i.e. PixDetailFragment)
    private boolean pictureSavedToGallery = false;  //Flag to indicate whether the "Save Picture to Gallery" button was pressed
    private boolean pictureDeleteSelected = false;  //Flag to indicate whether the "Delete Picture" button was pressed





    //=================== Define METHODS ===========================

    //Build encapsulating 'constructor'
    public static PictureViewDialogFragment newInstance(File pictureFile, String pixTitle, Date pixDate){

        //Create Bundle object (i.e. argument-bundle)
        Bundle argumentBundle = new Bundle();

        //Set key-value pairs for argument-bundle - picture File
        argumentBundle.putSerializable(ARGUMENT_PICTURE, pictureFile);

        //Set key-value pairs for argument-bundle - title
        argumentBundle.putString(ARGUMENT_TITLE, pixTitle);

        argumentBundle.putSerializable(ARGUMENT_DATE, pixDate);

        //Create PictureViewDialogFragment
        PictureViewDialogFragment pictureViewDialogFragment = new PictureViewDialogFragment();

        //Set argument-bundle for the PixDetailFragment
        pictureViewDialogFragment.setArguments(argumentBundle);

        //Return PictureViewDialogFragment object
        return pictureViewDialogFragment;
    }





    //Override lifecycle callback method from DialogFragment
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        super.onCreateDialog(savedInstanceState);

        //Get File 'value' from argument-bundle
        mPictureFile = (File) getArguments().getSerializable(ARGUMENT_PICTURE);

        //Get title 'value' from argument-bundle
        mPixTitle = (String) getArguments().getString(ARGUMENT_TITLE);

        //Get date 'value' from argument-bundle
        mPixDate = (Date) getArguments().getSerializable(ARGUMENT_DATE);

        //If pix title does not exist, is empty (i.e no characters), or contains only white-space
        if (mPixTitle == null || mPixTitle.isEmpty() || mPixTitle.trim().length()==0){
            mPixTitle = "Untitled";
        }

        //Obtain the Bitmap from the picture File
        Bitmap pictureBitmap = PictureUtility.getScaledBitmap(mPictureFile.getPath(), getActivity());

        //Rotate picture bitmap to correct orientation - as pictureBitmap is 90 degrees off-rotation
        Matrix matrix = new Matrix(); //Create Matrix object for transforming co-ordinates
        matrix.postRotate(90); //Set rotation for Matrix
        bitmapPictureCorrectOrientation = Bitmap.createBitmap(pictureBitmap , 0, 0, pictureBitmap .getWidth(), pictureBitmap.getHeight(), matrix, true); //Rotate picture Bitmap

        //Inflate ImageView dialog layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_picture, null);

        //Assign ImageView reference variable to associated resource ID
        final ImageView mImageView = (ImageView) view.findViewById(R.id.dialog_image_view);

        //Set 'scaled' picture Bitmap onto ImageView
        mImageView.setImageBitmap(bitmapPictureCorrectOrientation);




        //========================================= SET UP ALERT DIALOG ==================================================================
        //Set-up custom title to display in the dialog
        TextView dialogTitle = new TextView(getActivity()); //Create TextView object
        dialogTitle.setText(R.string.pix_picture_text); //Set curentDescriptionEditTextString on TextView
        dialogTitle.setTypeface(null, Typeface.BOLD);
        dialogTitle.setTextColor(getResources().getColor(R.color.colorButton)); //Set curentDescriptionEditTextString color
        dialogTitle.setTextSize(25); //Set size of curentDescriptionEditTextString
        dialogTitle.setBackgroundColor(getResources().getColor(R.color.yellow)); //Set curentDescriptionEditTextString background color


        //Create AlertDialog object
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setView(view); //Set view for the AlertDialog to encapsulate
        alertDialog.setCustomTitle(dialogTitle); //Set TITLE of dialog
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getResources().getString(android.R.string.cancel), //Set "ok" button
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Do nothing
                    }
                }
        );
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete Picture", //Set neutral button ("Save to Gallery")

                new DialogInterface.OnClickListener() {
                    //Set listener for neutral button ("Save to Gallery")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        pictureDeleteSelected = true; //Trigger flag to indicate "Delete Picture" button has been pressed
                        sendResult(Activity.RESULT_OK, pictureSavedToGallery, pictureDeleteSelected); //Send results of the "Picture Saved to Gallery" flag and "Delete Picture" flag to the target activity (i.e. PixListFragment)
                        pictureDeleteSelected = false; //Result the flag

                    }
                }
        );
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.save_to_gallery), //Set neutral button ("Save to Gallery")
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Set string for filename
                        String fileName = mPixTitle + "_" + mPixDate;

                        //Insert image to Gallery. Also, return a URL String - If the image failed to be saved in Gallery, the URL would be null
                        String savedImageUrl = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmapPictureCorrectOrientation, fileName, "");

                        //If the image file failed to be saved in Gallery
                        if (savedImageUrl == null) {
                            Toast.makeText(getActivity(), "Unable to save Picture to Gallery", Toast.LENGTH_LONG).show();
                            Toast.makeText(getActivity(), "Please check Storage Permissions", Toast.LENGTH_LONG).show();
                        }
                        //If the image file is SUCCESSFULLY saved to Gallery
                        else {
                            pictureSavedToGallery = true; //Trigger flag to indicate if "Save to GallerY' button has been pressed
                            sendResult(Activity.RESULT_OK, pictureSavedToGallery, pictureDeleteSelected); //Send results of the "Picture Saved to Gallery" flag and "Delete Picture" flag to the target activity (i.e. PixListFragment)
                            pictureSavedToGallery = false; //Resset the flag
                        }

                    }
                }
        );


        //Return the completed AlertDialog
        return alertDialog;
    }





    //Send boolean result for successful/unsuccessful Picture save to Gallery - back to target fragment (PixDetailFragment)
    // so that ULTIMATELY, we could display the Toast "Picture Saved to Gallery"
    private void sendResult(int resultCode, boolean pictureSavedToGallery, boolean pictureDeleteSelected){

        //If target fragment (PixDetailFragment) does NOT exist
        if (getTargetFragment() == null){
            return;
        }

        //Create Intent object
        Intent intent = new Intent();

        //Add key-value pair to the Intent
        intent.putExtra(EXTRA_PIX_PICTURE_SAVED_TO_GALLERY, pictureSavedToGallery);

        intent.putExtra(EXTRA_PIX_PICTURE_DELETE_SELECTED, pictureDeleteSelected);

        //Call onActivityResult(..) of target fragment (PixDetailFragment)
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }


}
