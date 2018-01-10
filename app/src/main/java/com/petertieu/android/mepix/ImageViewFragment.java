package com.petertieu.android.mepix;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Peter Tieu on 27/12/2017.
 */


//DialogFragment for ImageView dialog
public class ImageViewFragment extends DialogFragment{

    private static final String TAG = "ImageViewFragment";

    //Declare 'key' for argument bundle
    private static final String ARGUMENT_PICTURE = "image";

    //Declare ImageView view
    private ImageView mImageView;

    private Bitmap bitmapPictureCorrectOrientation;


    //Build encapsulating 'constructor'
    public static ImageViewFragment newInstance(File pictureFile){

        //Create Bundle object (i.e. argument-bundle)
        Bundle argumentBundle = new Bundle();

        //Set key-value pairs for argument-bundle
        argumentBundle.putSerializable(ARGUMENT_PICTURE, pictureFile);

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

        //Get 'value' from argument-bundle
        final File pictureFile = (File) getArguments().getSerializable(ARGUMENT_PICTURE);

        //Convert original picture from File to 'scaled' picture for use on dialog
        Bitmap pictureBitmap = PictureUtility.getScaledBitmap(pictureFile.getPath(), getActivity());

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

        //Return AlertDialog (subclass of Dialog), which sets the dialog properties
        return new AlertDialog.Builder(getActivity())
                .setView(view) //Set View of dialog
                .setTitle(R.string.pix_picture_text) //Set TITLE of dialog
                .setPositiveButton(android.R.string.ok, null) //Set "ok" button

                //Set listener for adding picture to gallery
                .setNeutralButton(R.string.save_to_gallery, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        Toast.makeText(getActivity(), "save to gallery clicked", Toast.LENGTH_LONG).show();

                        String capturePhotoUtils;

                        capturePhotoUtils = CapturePhotoUtils.insertImage(getContext().getContentResolver(), bitmapPictureCorrectOrientation, "helloTitle", "helloDescription");

                        CapturePhotoUtils.insertImage(getContext().getContentResolver(), bitmapPictureCorrectOrientation, "helloTitle", "helloDescription");

                        Toast.makeText(getActivity(), capturePhotoUtils, Toast.LENGTH_LONG).show();

                        MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmapPictureCorrectOrientation ,"helloTitle" , "helloDescription");

                        Toast.makeText(getActivity(), MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmapPictureCorrectOrientation ,"helloTitle" , "helloDescription"), Toast.LENGTH_LONG).show();


                        saveImage(bitmapPictureCorrectOrientation, "hello");



                    }
                })
                .create();
    }




    //Helper method for adding picture File to library
    private void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
//        String root = getActivity().getFilesDir().getAbsolutePath();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 10, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
