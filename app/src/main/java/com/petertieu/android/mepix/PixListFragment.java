package com.petertieu.android.mepix;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


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
    private PixAdapter mPixAdapter;


    private Callbacks mCallbacks;





    interface Callbacks{
        void onNewPix(Pix pix);
    }







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
            mPixRecyclerView.setAdapter(mPixAdapter);
        }
        else{

            mPixAdapter.setPixes(mPixes);

            mPixAdapter.notifyDataSetChanged();

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


                updateUI();

                mCallbacks.onNewPix(pix);



                return true;


            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }




    private class PixAdapter extends RecyclerView.Adapter<PixViewHolder>{

        //Declare List of Pix objects
        private List<Pix> mPixes;

        public PixAdapter(List<Pix> pixes){
            mPixes = pixes;
        }




        //
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




        @Override
        public void onBindViewHolder(PixViewHolder pixViewHolder, int position){

            Pix pix = mPixes.get(position);

            pixViewHolder.bind(pix);
        }




        public void setPixes(List<Pix> pixes){
            mPixes = pixes;
        }







    }



    private class PixViewHolder extends RecyclerView.ViewHolder{

        TextView mPixTitle;
        TextView mPixDescription;
        DateFormat mPixDate;

        Pix mPix;



        public PixViewHolder(View view){
            super(view);

            mPixTitle = (TextView) view.findViewById(R.id.pix_title);
            mPixDescription = (TextView) view.findViewById(R.id.pix_description);


            view.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view){

                }
            });



        }




        public void bind(Pix pix){
            mPix = pix;

            mPixTitle.setText(mPix.getTitle());
            mPixDescription.setText(mPix.getDescription());

            mPixDate = new DateFormat();

        }

    }





    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart() called");
    }



    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume() called");
        updateUI();
    }


    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause() called");
    }


    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop() called");
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
    }













}
