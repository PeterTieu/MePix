package com.petertieu.android.mepix.database;

//Database class #1:
//Set the blueprint for the SQL database of the Pix
public class PixDatabaseSchema {

    //STATIC inner class that sets the database table
    public static final class PixTable{

        //Deine the name of the table
        public static final String NAME = "pixes";

        //Inner-inner class that sets the names of the columns of the table
        public static final class Columns{
            public static final String ID = "id";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String FAVORITED = "favorited";
            public static final String ADDRESS = "location";
            public static final String LOCALITY = "locality";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String TAG = "tag";
            public static final String DESCRIPTION = "description";
        }
    }

}
