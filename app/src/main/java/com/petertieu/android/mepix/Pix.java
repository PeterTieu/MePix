package com.petertieu.android.mepix;

import java.util.Date;
import java.util.UUID;




//The blueprint of each Pix
public class Pix{

    //================ Declare Instance Variables ================================
    private UUID mId;               //UUID specific to each Pix
    private String mTitle;          //TITLE of the Pix
    private Date mDate;             //DATE of the Pix
    private boolean mFavorited;     //If Pix has been FAVORITED
    private String mDescription;    //TEXT in the Pix
    private String mAddress;        //ADDRESS of the Pix
    private String mLocality;       //LOCALITY of the Pix (i.e. region/suburb component of the address)
    private double mLatitude;       //LATITUDE of the Pix's location
    private double mLongitude;      //LONGITUDE of the Pix's location
    private String mTag;            //TAG in the Pix





    //================= Define Constructors ================================

    //Constructor #1
    public Pix(){
        //Create a UUID object that takes a random value, and pass it to Constructor #2
        //NOTE: Each Pix will be linked to a unique UUID value
        this(UUID.randomUUID());
    }

    //Constructor #2 - to be called in Constructor #1
    public Pix(UUID id){

        //Assign the (UUID) mId field
        mId = id;

        //Initialise the (Date) mDate field - so that pix.getDate.toString() in PixManager.getContentValues(..) would work.
        //NOTE: A new Date object is set to the 'current' date (and time) by default
        mDate = new Date();
    }





    //================= Define Getters and Setters ================================

    //Getter for UUID - NOTE: UUID has NO setter, as it cannot be set
    public UUID getId() {
        return mId;
    }


    //Getter for title
    public String getTitle() {
        return mTitle;
    }
    //Setter for title
    public void setTitle(String title) {
        mTitle = title;
    }


    //Getter for date
    public Date getDate() {
        return mDate;
    }
    //Setter for date
    public void setDate(Date date) {
        mDate = date;
    }


    //Getter for favorite
    public boolean isFavorited() {
        return mFavorited;
    }
    //Setter for favorite
    public void setFavorited(boolean favorited) {
        mFavorited = favorited;
    }


    //Getter for description
    public void setDescription(String description){
        mDescription = description;
    }
    //Setter for description
    public String getDescription(){
        return mDescription;
    }


    //Getter for address
    public String getAddress() {
        return mAddress;
    }
    //Setter for address
    public void setAddress(String address) {
        mAddress = address;
    }


    //Getter for locality (i.e. Suburb/City + State)
    public String getLocality() {
        return mLocality;
    }
    //Setter for locality
    public void setLocality(String locality) {
        mLocality = locality;
    }


    //Getter for latitude
    public double getLatitude() {
        return mLatitude;
    }
    //Setter for latitude
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }


    //Getter for longitude
    public double getLongitude() {
        return mLongitude;
    }
    //Setter for longitude
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }


    //Getter for tag
    public String getTag() {
        return mTag;
    }
    //Setter for tag
    public void setTag(String tag) {
        mTag = tag;
    }


    //Getter for file name of the Pix's picture - stored in the FileProvider
    //NOTE: Accessing Pix pictures, AND the FileProvider - have nothing to do with the SQLiteDatabase database!
    public String getPictureFilename(){
        return "IMG" + getId().toString() + ".jpg";
    }

}
