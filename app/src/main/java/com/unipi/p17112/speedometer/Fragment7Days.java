package com.unipi.p17112.speedometer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Fragment7Days extends Fragment {

    public Fragment7Days() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lastweek, container, false);
        //Set the recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //Open database connection
        SQLiteDatabase database = getActivity().openOrCreateDatabase("SpeedDB",android.content.Context.MODE_PRIVATE ,null);
        //Query that returns all the speed records from the last 7 days in descending order by the timestamp
        Cursor cursor = database.rawQuery("SELECT * FROM Speed WHERE datetime(timestamp) BETWEEN datetime('now','-6 days') AND datetime('now','localtime') ORDER BY timestamp DESC ;",null);
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