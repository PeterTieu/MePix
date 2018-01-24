package com.petertieu.android.mepix;

/**
 * Created by Peter Tieu on 13/01/2018.
 */


//Class that helps track the lifecycle state of PixViewPagerActivity activity
public class PixViewPagerActivityLifecycleTracker {

    //Declare static instance variable that is to act on whether PixViewPagerActivity activity is visible
    private static boolean activityVisible;

    //Getter for 'activityVisible'
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    //Setter for 'activityVisible' - Positive
    public static void activityResumed() {
        activityVisible = true;
    }

    //Setter for 'activityVisible' - Negative
    public static void activityPaused() {
        activityVisible = false;
    }

}
