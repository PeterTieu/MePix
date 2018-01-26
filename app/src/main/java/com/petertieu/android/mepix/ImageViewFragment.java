package com.petertieu.android.mepix;

import android.content.DialogInterface;
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


/**
 * Created by Peter Tieu on 27/12/2017.
 */


//DialogFragment for ImageView dialog
public class ImageViewFragment extends DialogFragment{

    private static final String TAG = "ImageViewFragment";

    //Declare 'key' for argument bundle
    private static final String ARGUMENT_PICTURE = "image";
    private static final String ARGUMENT_TITLE = "title";
    private static final String ARGUMENT_DATE = "date";

    //Declare ImageView view
    private ImageView mImageView;

    private Bitmap bitmapPictureCorrectOrientation;

    private File mPictureFile;
    private String mPixTitle;
    private Date mPixDate;


    //Build encapsulating 'constructor'
    public static ImageViewFragment newInstance(File pictureFile, String pixTitle, Date pixDate){

        //Create Bundle object (i.e. argument-bundle)
        Bundle argumentBundle = new Bundle();

        //Set key-value pairs for argument-bundle - picture File
        argumentBundle.putSerializable(ARGUMENT_PICTURE, pictureFile);

        //Set key-value pairs for argument-bundle - title
        argumentBundle.putString(ARGUMENT_TITLE, pixTitle);

        argumentBundle.putSerializable(ARGUMENT_DATE, pixDate);

        //Create ImageViewFragment
        ImageViewFragment imageViewFragment = new ImageViewFragment();

        //Set argument-bundle for the PixDetailFragment
        imageViewFragment.setArguments(argumentBundle);

        //Return ImageViewFragment object
        return imageViewFragment;


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

        //Convert original picture from File to 'scaled' picture for use on dialog
        Bitmap pictureBitmap = PictureUtility.getScaledBitmap(mPictureFile.getPath(), getActivity());

        //Rotate picture bitmap to correct orientation - as pictureBitmap is 90 degrees off-rotation
        Matrix matrix = new Matrix(); //Create Matrix object for transforming co-ordinates
        matrix.postRotate(90); //Set rotation for Matrix
        bitmapPictureCorrectOrientation = Bitmap.createBitmap(pictureBitmap , 0, 0, pictureBitmap .getWidth(), pictureBitmap.getHeight(), matrix, true); //Rotate picture Bitmap

        //Inflate ImageView dialog layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pix_picture, null);

        //Assign ImageView reference variable to associated resource ID
        ImageView mImageView = (ImageView) view.findViewById(R.id.dialog_image_view);

        //Set 'scaled' picture Bitmap onto ImageView
        mImageView.setImageBitmap(bitmapPictureCorrectOrientation);

        TextView dialogTitle = new TextView(getActivity());
        dialogTitle.setText(R.string.pix_picture_text);
        dialogTitle.setTypeface(null, Typeface.BOLD);
        dialogTitle.setTextColor(getResources().getColor(R.color.colorButton));
        dialogTitle.setTextSize(25);

        //Return AlertDialog (subclass of Dialog), which sets the dialog properties
        return new AlertDialog.Builder(getActivity())
                .setView(view) //Set View of dialog
                .setCustomTitle(dialogTitle) //Set TITLE of dialog
                .setPositiveButton(android.R.string.ok, null) //Set "ok" button


                //Set listener for adding picture to gallery
                .setNeutralButton(R.string.save_to_gallery, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
//

//                        Random randomNumberGenerator = new Random();
//
//                        int randomNumber = randomNumberGenerator.nextInt();

                        String fileName = mPixTitle + "_" + mPixDate;

                        String savedImageUrl;
                        savedImageUrl = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmapPictureCorrectOrientation , fileName , "");

                        if (savedImageUrl == null){
                            Toast.makeText(getActivity(), "Unable to save Picture to Gallery", Toast.LENGTH_LONG).show();
                            Toast.makeText(getActivity(), "Please check Storage Permissions", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getActivity(), "Picture saved to Gallery", Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .create();
    }



}
