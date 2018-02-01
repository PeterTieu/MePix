package com.petertieu.android.mepix;

//Class that helps track the lifecycle state of PixViewPagerActivity activity.
//This is so that we could determine when the hosting activity of PixDetailFragment is PixViewPagerActivity OR PixListActivity.
//The hosting activity of PixDetailFragment is...
    //1: PixViewPagerActivity: When we are in one-pane layout/mode
    //2: PixListActivity: When we are in two-pane layout/mode
//Ultimately, we need to know when PixViewPagerActivity is the hosting activity of the PixDetailFragment fragment so that
// when the "Delete Pix" menu item is pressed while in detail view (i.e. PixDetailFragment),
// we want to only call getActivity.finish() in PixDetailFragment IF AND ONLY IF the hosting activity is PixViewPagerActivity.. NOT PixListActivity.
//This is because if getActivity.finish() is called in PixDetaiFragment and the hosting activity is PixViewPagerActivity,
// the hosting activity (PixViewPagerActivity) will pop off the stack, thereby revealing the PixListActivity (containing the PixListFragment).
// IOW, we go from the detail view to the list view. This is OK.
// Whereas, if we were in two-pane mode (i.e. sw > 600dp), and the hosting activity of PixDetailFragment is PixListActivity,
// when we call getActivity.finish(), PixListActivity will pop off... and since it's the only activity in the stack, the whole app will close!
// IOW, we would essentially close the app everytime a Pix is deleted whilst in two-pane layout/mode - which is not desired!
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
