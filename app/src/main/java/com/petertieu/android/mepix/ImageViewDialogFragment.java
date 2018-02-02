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
public class ImageViewDialogFragment extends DialogFragment{


    //=================== Declare INSTANCE VARIABLES ===========================

    //Declare 'key' for argument bundle
    private static final String ARGUMENT_PICTURE = "image"; //Key for picture File
    private static final String ARGUMENT_TITLE = "title";   //Key for Pix title
    private static final String ARGUMENT_DATE = "date";     //Key for Pix date

    public static final String EXTRA_PIX_PICTURE_SAVED_TO_GALLERY = "pictureSaved";


    //Rotated version of the Bitmap generated from PictureUtility (in the correct orientation)
    private Bitmap bitmapPictureCorrectOrientation;


    private File mPictureFile;  //Pix File (to display in ImageView)
    private String mPixTitle;   //Pix title (to display in the filename of the picture file saved to Gallery)
    private Date mPixDate;      //Pix date ((to display in the filename of the picture file saved to Gallery)





    //=================== Define METHODS ===========================

    //Build encapsulating 'constructor'
    public static ImageViewDialogFragment newInstance(File pictureFile, String pixTitle, Date pixDate){

        //Create Bundle object (i.e. argument-bundle)
        Bundle argumentBundle = new Bundle();

        //Set key-value pairs for argument-bundle - picture File
        argumentBundle.putSerializable(ARGUMENT_PICTURE, pictureFile);

        //Set key-value pairs for argument-bundle - title
        argumentBundle.putString(ARGUMENT_TITLE, pixTitle);

        argumentBundle.putSerializable(ARGUMENT_DATE, pixDate);

        //Create ImageViewDialogFragment
        ImageViewDialogFragment imageViewDialogFragment = new ImageViewDialogFragment();

        //Set argument-bundle for the PixDetailFragment
        imageViewDialogFragment.setArguments(argumentBundle);

        //Return ImageViewDialogFragment object
        return imageViewDialogFragment;
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

        //Set-up custom title to display in the dialog
        TextView dialogTitle = new TextView(getActivity()); //Create TextView object
        dialogTitle.setText(R.string.pix_picture_text); //Set text on TextView
        dialogTitle.setTypeface(null, Typeface.BOLD);
        dialogTitle.setTextColor(getResources().getColor(R.color.colorButton)); //Set text color
        dialogTitle.setTextSize(25); //Set size of text
        dialogTitle.setBackgroundColor(getResources().getColor(R.color.yellow)); //Set text background color


        //Return AlertDialog (subclass of Dialog), which sets the dialog properties
        return new AlertDialog.Builder(getActivity())
                .setView(view) //Set View of dialog
                .setCustomTitle(dialogTitle) //Set TITLE of dialog
                .setPositiveButton(android.R.string.ok, null) //Set "ok" button
                .setNeutralButton(R.string.save_to_gallery, //Set neutral button ("Save to Gallery")
                        //Set listener for neutral button ("Save to Gallery")
                        new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //Set string for filename
                                String fileName = mPixTitle + "_" + mPixDate;

                                //Insert image to Gallery.
                                // Also, return a URL String. If the image failed to be saved in Gallery, the URL would be null
                                 String savedImageUrl = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmapPictureCorrectOrientation, fileName, "");

                                //If the image file failed to be saved in Gallery
                                if (savedImageUrl == null) {
                                    Toast.makeText(getActivity(), "Unable to save Picture to Gallery", Toast.LENGTH_LONG).show();
                                    Toast.makeText(getActivity(), "Please check Storage Permissions", Toast.LENGTH_LONG).show();
                                }
                                //If the image file is SUCCESSFULLY saved to Gallery
                                else {
                                    boolean pictureSavedToGallery = true;
                                    sendResult(Activity.RESULT_OK, pictureSavedToGallery);
//                                    Toast.makeText(getActivity(), "Picture saved to Gallery", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .create();
    }





    //Send boolean result for successful/unsuccessful Picture save to Gallery - back to target fragment (PixDetailFragment)
    private void sendResult(int resultCode, boolean pictureSavedToGallery){

        //If target fragment (PixDetailFragment) does NOT exist
        if (getTargetFragment() == null){
            return;
        }

        //Create Intent object
        Intent intent = new Intent();

        //Add key-value pair to the Intent
        intent.putExtra(EXTRA_PIX_PICTURE_SAVED_TO_GALLERY, pictureSavedToGallery);

        //Call onActivityResult(..) of target fragment (PixDetailFragment)
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

}
