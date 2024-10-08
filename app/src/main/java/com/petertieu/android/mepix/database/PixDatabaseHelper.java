package com.petertieu.android.mepix.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;




//Database class #2:
//Create the database by using methods from the SQLiteOpenHelper class (onCreate() and onUpgrade())
public class PixDatabaseHelper extends SQLiteOpenHelper{

    //Define instance variables
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "pixDatabase.db";


    //Build constructor
    public PixDatabaseHelper(Context context){
        //Assign the instance variables inherited from the SQLiteOpenHelper class
        super(context, DATABASE_NAME, null, VERSION);
    }



    //Low-level method for creating the SQLiteDatabase database
    @Override
    public void onCreate(SQLiteDatabase sqlLiteDb){

        //Create the database.
        //Define the names of each column in the database. Each column corresponds to a field of the Pix (i.e. instance variable (state))
        sqlLiteDb.execSQL(
                "create table " +
                        PixDatabaseSchema.PixTable.NAME +
                        "(" +
                        " _id integer primary key autoincrement, " +
                        PixDatabaseSchema.PixTable.Columns.ID + ", " +
                        PixDatabaseSchema.PixTable.Columns.TITLE + ", " +
                        PixDatabaseSchema.PixTable.Columns.DATE + ", " +
                        PixDatabaseSchema.PixTable.Columns.FAVORITED + ", " +
                        PixDatabaseSchema.PixTable.Columns.ADDRESS + ", " +
                        PixDatabaseSchema.PixTable.Columns.LOCALITY + ", " +
                        PixDatabaseSchema.PixTable.Columns.LATITUDE + ", " +
                        PixDatabaseSchema.PixTable.Columns.LONGITUDE + ", " +
                        PixDatabaseSchema.PixTable.Columns.TAG + ", " +
                        PixDatabaseSchema.PixTable.Columns.DESCRIPTION +
                        ")"
        );
    }




    //Low-level method for upgrading the database's version
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion){
        //Do nothing

    }
}
