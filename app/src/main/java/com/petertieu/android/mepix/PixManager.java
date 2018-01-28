package com.petertieu.android.mepix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.petertieu.android.mepix.database.PixCursorWrapper;
import com.petertieu.android.mepix.database.PixDatabaseHelper;
import com.petertieu.android.mepix.database.PixDatabaseSchema;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Peter Tieu on 7/12/2017.
 */


//The SINGLETON which manages Pixes.
//Its functions are:
    //1: ADDS Pixes to the list of Pixes (and stores their instance variable (state) values into SQLiteDatabase)
    //2: REMOVES Pixes from the list of Pixes (and removes their instance variable (state) values from SQLiteDatabase)
    //3: ALLOWS access of specific Pixes from the list of Pixes (and queries their instance variable (state) values from SQLiteDatabase)
public class PixManager {

    //Declare the PixManager SINGLETON - i.e. there is only ONE of this object for this entire class
    private static PixManager sPixManager;

    //Declare a SQLiteDatabase SINGLETON = i.e. there is only ONE of this object for this entire class
    private static SQLiteDatabase mSQLiteDatabase;

    //Declare a Context
    private Context mContext;




    //================= Build static 'constructors' ================================

    //Static 'constructor' #1
    public static PixManager get(Context context){

        //If the PixManager object does NOT exist
        if (sPixManager == null){
            //Create the PixManager (singleton) object
            return new PixManager(context);
        }

        //Return the PixManager object (whether existing or newly created)
        return sPixManager;
    }


    //Static 'constructor' #2
    private PixManager(Context context){

        //Create a Context that is tied to the LIFECYCLE of the ENTIRE application (instead of activity)
        // for the purpose of retaining the SQLiteDatabase
        mContext = context.getApplicationContext();

        //Create/Retrieve the SINGLETON database (of type SQLiteDatabase)
        //getWritableDatable will:
            //IF: An SQLiteDatabase does NOT exist..
                //Call the overriden onCreate(SQLiteDatabase) from PixDatabaseHelper to create the SQLiteDatabase
            //IF: An SQLiteDatabase EXISTS...
                //Check the version number of the database and call the overriden onUpgrade(..) from PixDatabaseHelper to upgrade if necessary
        mSQLiteDatabase = new PixDatabaseHelper(mContext).getWritableDatabase();
    }





    //===================== All the following methods are accessed like so: PixManager.get(context).*method* ================================
    //===================== Their puroses are to QUERY, WRITE and REMOVE to/from the SQLiteDatabase database ===============================

    //QUERY method: Return a SPECIFIC Pix from the SQLiteDatabase, "pixes"
    public Pix getPix(UUID id){

        //Create a PixCursorWrapper using the queryPixes(..) helper method, taking into account both whereClause and whereArgs
        PixCursorWrapper pixCursor = queryPixes(PixDatabaseSchema.PixTable.Columns.ID + " = ?", new String[]{id.toString()});

        //Use try-block, as pixCursor (CursorWrapper) may throw RuntimeException if database doesn't exist
        try{
            if (pixCursor.getCount() == 0){
                return null;
            }

            //There is only ONE item selected, so moveToFirst() selects that one and only item
            pixCursor.moveToFirst();

            //Get the Pix from the SQLiteDatabase
            return pixCursor.getPixFromDatabase();
        }
        finally {
            //Close the cursor, releasing all of its resources and making it invalid
            pixCursor.close();
        }
    }





    //QUERY method: Return the ENTIRE List of Pix objects from the SQLiteDatabase, "pixes"
    public List<Pix> getPixes(){

        //Declare the List of Pixes
        List<Pix> pixes = new ArrayList<>();

        //Create a PixCursorWrapper using the queryPixes(..) helper method
        PixCursorWrapper pixCursor = queryPixes(null, null);

        //Use try-block, as pixCursor (CursorWrapper) may throw RuntimeException if database doesn't exist
        try{
            //Move cursor to first row
            pixCursor.moveToFirst();

            //While the cursor hasn't reached the last row yet
            while(!pixCursor.isAfterLast()){

                //Add all data from the row that the cursor is on.. to.. the "pixes" list
                pixes.add(pixCursor.getPixFromDatabase());

                //Move the cursor to the next row
                pixCursor.moveToNext();
            }
        }
        finally {
            //Close the cursor, releasing all of its resources and making it invalid
            pixCursor.close();
        }

        //Return the entire list of Pix objects
        return pixes;
    }





    //WRITE method: Add a Pix to the SQLiteDatabase, "pixes"
    public void addPix(Pix pix){

        //Call the getContentValues(Pix) helper method to obtain a ContentValues object. The ContentValues object acts a 'buffer' data storage for data from the Pix object
        ContentValues contentValues = getContentValues(pix);

        //Insert data... FROM: the ContentValues object...  TO: the SQLiteDatabase, "pixes".
        mSQLiteDatabase.insert(PixDatabaseSchema.PixTable.NAME, null, contentValues);
    }





    //Delete Pix from SQLiteDatabase, "pixes"
    public void deletePix(Pix pix){

        //Get Pix mId instance variable in String form
        String pixIdString = pix.getId().toString();

        //Delete Pix from SQLiteDatabase
        mSQLiteDatabase.delete(PixDatabaseSchema.PixTable.NAME, PixDatabaseSchema.PixTable.Columns.ID + " = ? ", new String[]{pixIdString});
    }





    //Update Pix on the database.
    // This method is called whenever a change occurs to a field in a Pix object,
    // e.g. when these methods are called mPix.setTitle(..), mPix.setAddress(..), etc, the Pix object has been changed.
    //This method MUST be called after a change occurs to a Pix, so that the new stored value/change could be added to the SQLiteDatabase!
    public void updatePixOnDatabase(Pix pix){

        //Get the specific Pix ID in String form
        String pixId = pix.getId().toString();

        //Create a ContentValue and store data from the specific Pix
        ContentValues contentValues = getContentValues(pix);

        //UPDATE the database with data from the specific pix
        mSQLiteDatabase.update(
                PixDatabaseSchema.PixTable.NAME, //(String) Name of database to update
                contentValues, //(ContentValues) Data to be added to the database
                PixDatabaseSchema.PixTable.Columns.ID + " = ? ", //(String) whereClause - Which column to select
                new String[]{pixId} //(String) whereArgs - Which value to add to the selected column
        );

    }





    //============= Define HELPER METHODS ==============================================================================================

    //QUERY data from the SQLiteDatabase
    private PixCursorWrapper queryPixes(String whereClause, String[] whereArgs){

        //Return a cursor, using query(..) from SQLiteDatabase
        Cursor pixCursor = mSQLiteDatabase.query(
                PixDatabaseSchema.PixTable.NAME, //(String) Name of table to query from
                null, //(String[]) List of columns to select. "null" selects all columns
                whereClause, //(String) Which column to select. "null" returns all columns
                whereArgs, //(String) Which value to return in the selected column
                null, //(String) How rows are grouped. "null" causes the rows not to be grouped
                null, //(String) Which rows groups are selected. "null" selects all row groups
                null //(Sting) How to order the rows. "null" uses the default sort order
        );

        //Pass the Cursor into the PixCursorWrapper constructor
        return new PixCursorWrapper(pixCursor);
    }





    //WRITE data from the Crime object to a ContentValues object. The ContentValues object acts as a 'buffer' data storage for data from the Pix object
    private static ContentValues getContentValues(Pix pix){

        //Create a ContentValues object
        ContentValues contentValues = new ContentValues();

        //Put value... FROM: the Pix object (argument 2)... TO: The SQLiteDatabase (argument 1)
        contentValues.put(PixDatabaseSchema.PixTable.Columns.ID, pix.getId().toString());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.TITLE, pix.getTitle());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.DATE, pix.getDate().getTime());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.FAVORITED, pix.isFavorited() ? 1:0); //IF crime.isFavorited() == true, put 1. Else, put 0
        contentValues.put(PixDatabaseSchema.PixTable.Columns.ADDRESS, pix.getAddress());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.LOCALITY, pix.getLocality());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.LATITUDE, pix.getLatitude());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.LONGITUDE, pix.getLongitude());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.TAG, pix.getTag());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.DESCRIPTION, pix.getDescription());

        //Return the ContentValues object
        return contentValues;
    }





    //======================== READ from the FileProvider ===========================================================================
    //NOTE; Accessing picture files AND the FileProvider.. have got nothing to do with the SQLiteDatabase database!

    //Get picture file of the Pix via its file location
    public File getPictureFile(Pix pix){

        //Return absolute path to the directory on the filesystem.
        //Do this by accessing mContext, which is a Context object - it lasts for the ENTIRE lifecycle of the app, NOT the activity.
        //Therefore, accessing the file directory via the Context object is the appropriate approach
        File filesDirectory = mContext.getFilesDir();

        //Return File object
        return new File(filesDirectory, pix.getPictureFilename());
    }

}
