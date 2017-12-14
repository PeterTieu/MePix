package com.petertieu.android.mepix;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Peter Tieu on 7/12/2017.
 */


//The blueprint of each Pix
    
public class Pix {


    ///================= Declare Instance Variables ================================
    private UUID mId;               //UUID specific to each Pix
    private String mTitle;          //TITLE of the Pix
    private Date mDate;             //DATE of the Pix
    private boolean mFavorited;     //If Pix has been FAVORITED
    private String mTagged;         //TAGGED in the Pix
    private String mDescription;           //TEXT in the Pix



    //================= Define Constructors ================================

    //Contructor #1
    public Pix(){
        this(UUID.randomUUID());
    }

    //Constructor #2
    public Pix(UUID id){
        mId = id;
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

    public String getTagged() {
        return mTagged;
    }

    public void setTagged(String tagged) {
        mTagged = tagged;
    }

    public void setDescription(String description){
        mDescription = description;
    }

    public String getDescription(){
        return mDescription;
    }


}
