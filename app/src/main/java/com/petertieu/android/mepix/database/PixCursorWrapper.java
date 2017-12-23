package com.petertieu.android.mepix.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.petertieu.android.mepix.Pix;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Peter Tieu on 9/12/2017.
 */


//Wrap the Cursor and add more methods on top of the Cursor.
//The Cursor reads the data from the data table, "pixes"
public class PixCursorWrapper extends CursorWrapper{


    //Build constructor
    public PixCursorWrapper(Cursor cursor){
        //Assign the instance variable, "cursor" from the CursorWrapper class
        super(cursor);
    }



    //Read the columns from the "pixes" table, and return the Pix object using a Cursor
    public Pix getPixFromDatabase(){

        //Pull the data from the column
        String id = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.ID));
        String title = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.TITLE));
        long date = getLong(getColumnIndex(PixDatabaseSchema.PixTable.Columns.DATE));
        int favorited = getInt(getColumnIndex(PixDatabaseSchema.PixTable.Columns.FAVORITED));
        String tag = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.TAG));
        String description = getString(getColumnIndex(PixDatabaseSchema.PixTable.Columns.DESCRIPTION));

        //Assign data to the instance variables of the Pix object
        Pix pix = new Pix(UUID.fromString(id));
        pix.setTitle(title);
        pix.setDate(new Date(date));
        pix.setFavorited(favorited != 0);
        pix.setTag(tag);
        pix.setDescription(description);

        return pix;
    }
}
