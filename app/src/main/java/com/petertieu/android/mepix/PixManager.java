package com.petertieu.android.mepix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.petertieu.android.mepix.database.PixCursorWrapper;
import com.petertieu.android.mepix.database.PixDatabaseHelper;
import com.petertieu.android.mepix.database.PixDatabaseSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Peter Tieu on 7/12/2017.
 */


//The SINGLETON which manages Pixes
    //1: ADDS Pixes to the list of Pixes
    //2: REMOVES Pixes from the list of Pixes
    //3:
public class PixManager {

    //Declare the PixManager SINGLETON
    private static PixManager sPixManager;

    //Declare a Context
    private Context mContext;

    //Declare a SQLiteDatabase
    private static SQLiteDatabase mSQLiteDatabase;




    //Create the PixManager object
    public static PixManager get(Context context){
        if (sPixManager == null){
            return new PixManager(context);
        }
        else{
            return sPixManager;
        }
    }




    //Constructor called by get(..)
    private PixManager(Context context){

        //Create a Context that is tied to the lifecycle of the entire application (instead of activity)
        mContext = context.getApplicationContext();

        //Create a new database of type SQLiteDatabase
        //getWritableDatable will:
            //IF: An SQLiteDatabase does NOT exist..
                //Call the overriden onCreate(SQLiteDatabase) from PixDatabaseHelper to create the SQLiteDatabase
            //IF: An SQliteDatabase EXISTS...
                //Chekc the version number of the database and call the overriden onUpgrade(..) from PixDatabaseHelper to upgrade if necessary
        mSQLiteDatabase = new PixDatabaseHelper(mContext).getWritableDatabase();
    }




    //Return the ENTIRE List of Pix objects from the SQliteDatabase, "pixes"
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




    //Return a SPECIFIC Pix from the SQLiteDatabase, "pixes"
    public Pix getCrime(UUID id){

        //Create a PixCursorWrapper using the queryPixes(..) helper method, takig into accoutn both whereClause and whereArgs
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




    //Add a Pix to the SQLiteDatabase, "pixes"
    public void addPix(Pix pix){

        //Call the getContentValues(Pix) helper method to obtain a ContentValues object. The ContentValues object acts a 'buffer' data storage for data from the Pix object
        ContentValues contentValues = getContentValues(pix);

        //Insert data... FOMR: the ContentValues object...  TO: the SQLiteDatabase, "pixes".
        mSQLiteDatabase.insert(PixDatabaseSchema.PixTable.NAME, null, contentValues);
    }





















    //============= Define HELPER METHODS ==============================================================================================

    //WRITE data from the Crime object to a ContentValues object. The ContentValues object acts as a 'buffer' data storage for data from the Pix object
    private ContentValues getContentValues(Pix pix){

        //Create a ContentValues object
        ContentValues contentValues = new ContentValues();

        //Put value... FROM: the Pix object (argument 2)... TO: The SQLiteDatabase (argument 1)
        contentValues.put(PixDatabaseSchema.PixTable.Columns.ID, pix.getId().toString());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.TITLE, pix.getTitle());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.DATE, pix.getDate().toString());
        contentValues.put(PixDatabaseSchema.PixTable.Columns.FAVORITED, pix.isFavorited() ? 1:0); //IF crime.isFavorited() == true, put 1. Else, put 0
        contentValues.put(PixDatabaseSchema.PixTable.Columns.TAGGED, pix.getTagged());

        //Return the ContentValues object
        return contentValues;
    }





    //READ data from the SQLiteDatabase
    private PixCursorWrapper queryPixes(String whereClause, String[] whereArgs){

        //Return a cursor, using query(..) from SQLiteDatabase
        Cursor pixCursor = mSQLiteDatabase.query(
                PixDatabaseSchema.PixTable.NAME, //(String) Name of table to query from
                null, //(String[]) List of columns to select. "null" selects all columns
                whereClause, //(String) Which row to return in the selected column(s). "null" returns all rows in the selected column(s)
                whereArgs, //(String) Which value to return
                null, //(String) How rows are grouped. "null" causes the rows not to be grouped
                null, //(String) Which rows groups are selected. "null" selects all row groups
                null //(Sting) How to order the rows. "null" uses the default sort order
        );

        //Pass the Cursor into the PixCursorWrapper constructor
        return new PixCursorWrapper(pixCursor);

    }



}
