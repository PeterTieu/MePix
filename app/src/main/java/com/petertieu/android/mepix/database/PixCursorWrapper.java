package com.petertieu.android.mepix.database;

import android.database.Cursor;
import android.database.CursorWrapper;
import com.petertieu.android.mepix.Pix;
import java.util.Date;
import java.util.UUID;




//Database class #3:
//Class for QUERYING the SQLiteDatabase - it is called by the PixManager's methods: getPix(..) and getPixes(..)
//Wrap the Cursor and add more methods on top of the Cursor.
//The Cursor READS the data from the data table, "pixes", in "pixDatabase.db"
public class PixCursorWrapper extends CursorWrapper{



    //Build constructor
    public PixCursorWrapper(Cursor cursor){
        //Assign the instance variable, "cursor" from the CursorWrapper class
        super(cursor);
    }




    //READ data stored in the columns of the "pixes" table, and then set it to the Pix object (of a specific UUID)
    public Pix getPixFromDatabase(){

        //Pull the data from the column
        String id = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.ID));
        String title = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.TITLE));
        long date = getLong(getColumnIndex(PixDatabaseSchema.PixTable.Columns.DATE));
        int favorited = getInt(getColumnIndex(PixDatabaseSchema.PixTable.Columns.FAVORITED));
        String address = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.ADDRESS));
        String locality = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.LOCALITY));
        double latitude = getDouble(getColumnIndex(PixDatabaseSchema.PixTable.Columns.LATITUDE));
        double longitude = getDouble(getColumnIndex(PixDatabaseSchema.PixTable.Columns.LONGITUDE));
        String tag = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.TAG));
        String description = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.DESCRIPTION));

        //Assign data to the instance variables of the Pix object
        Pix pix = new Pix(UUID.fromString(id));
        pix.setTitle(title);
        pix.setDate(new Date(date));
        pix.setFavorited(favorited != 0);
        pix.setAddress(address);
        pix.setLocality(locality);
        pix.setLatitude(latitude);
        pix.setLongitude(longitude);
        pix.setTag(tag);
        pix.setDescription(description);

        //Return the pix
        return pix;
    }

}
