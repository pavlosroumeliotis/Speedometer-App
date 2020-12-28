package com.unipi.p17112.speedometer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class FragmentAll extends Fragment {

    public FragmentAll() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all, container, false);
        //Set the recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //Open database connection
        SQLiteDatabase database = getActivity().openOrCreateDatabase("SpeedDB",android.content.Context.MODE_PRIVATE ,null);
        //Query that returns all the speed records in descending order by the timestamp
        Cursor cursor = database.rawQuery("SELECT * FROM Speed ORDER BY timestamp DESC",null);
        //List that will contains all the results from the query
        ArrayList<SpeedRecord> speeds = new ArrayList<>();
        //Read every speed record from the result and add them to the list
        if (cursor.getCount() > 0){
            while(cursor.moveToNext()){
                SpeedRecord speedRecord = new SpeedRecord(cursor.getDouble(1),cursor.getDouble(0),cursor.getDouble(3),
                        cursor.getInt(4),cursor.getString(2));
                speeds.add(speedRecord);
            }
        }
        //Connect the list to the recycler view
        recyclerView.setAdapter(new RecyclerViewAdapter(speeds));
        return view;
    }
}