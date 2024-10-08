package com.petertieu.android.mepix;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;




//Utility class for:
    //1: Take as INPUT: picture File URI... then return as OUTPUT: Bitmap
    //2: 'Scaling' picture Bitmap for PictureViewDialogFragment dialog fragment, in case height/width of picture Bitmap is greater than allowable by the ImageView dialog fragment
//NOTE: A utility class is one that contains helper methods for other classes, and therefore acts a 'helping' class. It shouldn't contain instance variables, as per conventional object classes.
public class PictureUtility {


    //Static method for getting picture File URI and returning Bitmap
    public static Bitmap getScaledBitmap(String path, Activity activity){

        //Create Point object to hold 'x' and 'y' co-ordinates
        Point size = new Point();

        //Get default display size of activity, in this case, the ImageView dialog fragment (i.e. default maximum size for a Bitmap)
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        //Call overloaded method getScaledBitmap(..)
        //NOTE: 'size.x' and 'size.y' are max. co-ordinates of activity
        return getScaledBitmap(path, size.x, size.y);
    }





    //Overloaded method - called by method above
    public static Bitmap getScaledBitmap(String path, int maxPictureWidth, int maxPictureHeight){

        //Create BitmapFactory.Options object, a nested class of BitmapFactory
        //NOTE: BitmapFactory is a class that creates Bitmap objects
        BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();

        //Tell decoder there is no bitmap present
        bitmapFactoryOptions.inJustDecodeBounds = true;

        //Extract Bitmap object from path of picture File, then assign it to BitmapFactory.Options reference variable (bitmapFactoryOptions)
        BitmapFactory.decodeFile(path, bitmapFactoryOptions);

        //Get height and width of 'original'/'un-scaled' picture Bitmap
        float originalPictureHeight = bitmapFactoryOptions.outHeight;
        float originalPictureWidth = bitmapFactoryOptions.outWidth;


        //Define 'final' scale factor of picture Bitmap (NOTE: If scaleFactor=2, then width and height of picture Bitmap are halved)
        int scaleFactor = 1;

        //If original picture Bitmap (from File) has height or width GREATER THAN max. height or width, respectively
        if (originalPictureHeight > maxPictureHeight || originalPictureWidth > maxPictureWidth){

            //Obtain scale factors for height and widths - which are functions of the original height/width and the max. height/width
            float heightScaleFactor = originalPictureHeight / maxPictureHeight;
            float widthScaleFactor = originalPictureWidth / maxPictureWidth;

            //Obtain 'final' scale factor to apply for height and width. Take 'final' scale factor as greatest factor between height and width. Will be greater than 1
            scaleFactor = Math.round(heightScaleFactor > widthScaleFactor ? heightScaleFactor : widthScaleFactor);
        }


        //Reassign bitmapFactoryOptions reference variable to new BitmapFactory.Options object
        bitmapFactoryOptions = new BitmapFactory.Options();

        //Assign instance variable of BitmapFactory.Options object to 'final' scale factor
        bitmapFactoryOptions.inSampleSize = scaleFactor;

        //Extract Bitmap object from path of picture File, then assign it to BitmapFactory.Options reference variable (bitmapFactoryOptions),
        // which may now have a NEW scale factor
        return BitmapFactory.decodeFile(path, bitmapFactoryOptions);
    }

}
