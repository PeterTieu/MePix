package com.petertieu.android.mepix;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;




//Define abstract activity to be subclassed by other activities
public abstract class OneFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the activity content to an explicit view
        //NOTE: getLayoutResId is to be overriden in the subclass to return reference resource files based on the configuration qualifier
        setContentView(getLayoutResId());

        //Return the FragmentManager for interacting with fragments associated with this activity
        FragmentManager fm = getSupportFragmentManager();

        //Find a fragment that was identified by the given ID
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        //If the fragment doesn't exist yet
        if (fragment == null){
            //Create a fragment
            fragment = createFragment();
            //Start a series of edit operations on the Fragments associated with this FragmentManager
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

    }





    //Declare the abstract method that is to be overriden in the subclass activity class
    protected abstract Fragment createFragment();





    //To be overriden in the subclass activity class - to return reference resource files
    @LayoutRes
    protected abstract int getLayoutResId();

}