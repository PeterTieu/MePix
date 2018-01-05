package com.petertieu.android.mepix;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Peter Tieu on 7/12/2017.
 */


//The blueprint of each Pix
    
public class Pix {

    //================ Declare Instance Variables ================================
    private UUID mId;               //UUID specific to each Pix
    private String mTitle;          //TITLE of the Pix
    private Date mDate;             //DATE of the Pix
    private boolean mFavorited;     //If Pix has been FAVORITED
    private String mTag;         //TAG in the Pix
    private String mDescription;    //TEXT in the Pix




    //================= Define Constructors ================================

    //Contructor #1
    public Pix(){
        this(UUID.randomUUID());
    }



    //Constructor #2
    public Pix(UUID id){

        //Assign the (UUID) mId field
        mId = id;

        //Initialise the (Date) mDate field - so that pix.getDate.toString() in PixManager.getContentValues(..) would work
        mDate = new Date();
    }




    //================= Define Getters and Setters ================================

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isFavorited() {
        return mFavorited;
    }

    public void setFavorited(boolean favorited) {
        mFavorited = favorited;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public void setDescription(String description){
        mDescription = description;
    }

    public String getDescription(){
        return mDescription;
    }

    public String getPictureFilename(){
        return "IMG" + getId().toString() + ".jpg";
    }
}
