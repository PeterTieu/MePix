package com.petertieu.android.mepix;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Toast;

import java.util.List;


/**
 * Created by Peter Tieu on 7/12/2017.
 */

//Activity hosting PixList
public class PixListFragment extends Fragment{

    //Define Log identifier
    private final String TAG = "PixListFragment";

    //Declare the RecyclerView
    private RecyclerView mPixRecyclerView;

    //Declare the Adapter
    private Adapter mPixAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Log lifecycle callback
        Log.i(TAG, "onCreate(..) called");

        //Report that this fragment would like to participate in populate menus
        setHasOptionsMenu(true);
    }




    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState){
        super.onCreateView(layoutInflater, viewGroup, savedInstanceState);

        //Log lifecycle callback
        Log.i(TAG, "onCreateView(..) called");

        //Create View object
        View view = layoutInflater.inflate(R.layout.fragment_pix_list, viewGroup, false);

        //Create RecyclerView for the LIST VIEW
        mPixRecyclerView = (RecyclerView) view.findViewById(R.id.pix_list_recycler_view);

        //Set layout for the RecyclerView
        mPixRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Create the Adapter and link it with the RecyclerView
        updateUI();

        return view;
    }




    //Helper method for creating the Adapter and linking it with the RecyclerView
    public void updateUI(){

        PixManager pixManager = PixManager.get(getActivity());

        List<Pix> mPixes = pixManager.getPixes();

        if (mPixAdapter == null){
            mPixAdapter = new PixAdapter(mPixes);
        }

    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu, menuInflater);

        //Log lifecycle callback
        Log.i(TAG, "onCreateOptionsMenu(..) called");

        //Inflate a menu hiearchy from specified resource
        menuInflater.inflate(R.menu.fragment_pix_list, menu);

    }




    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        switch(menuItem.getItemId()){
            case(R.id.new_pix):

                //Create a Pix object
                Pix pix = new Pix();

                //Add the Pix object to the SQLiteDatabase, "pixes"
                PixManager.get(getActivity()).addPix(pix);

                return true;


            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }



    private class PixAdapter extends RecyclerView.Adapter<PixHolder>{

        private List<Pix> mPixes;


        public PixAdapter(List<Pix> pixes){
            mPixes = pixes;
        }


        @Override
        public int getItemCount(){
            return mPixes.size();
        }

        @Override
        public PixViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            View view = layoutInflater.inflate(R.layout.list_item_pix, viewGroup, false);

            return new PixViewHolder(view);
        }



    }



    private class PixViewHolder extends RecyclerView.ViewHolder{


        @Override
        public PixViewHolder()

    }













}
