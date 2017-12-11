package com.petertieu.android.mepix.database;

/**
 * Created by Peter Tieu on 9/12/2017.
 */

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
            public static final String TAGGED = "tagged";
            public static final String TEXT = "text";
        }
    }

}
