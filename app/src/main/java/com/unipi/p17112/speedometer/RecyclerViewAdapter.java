package com.unipi.p17112.speedometer;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    ArrayList<SpeedRecord> speedRecords;
    public RecyclerViewAdapter(ArrayList<SpeedRecord> speedRecords){
        this.speedRecords = speedRecords;
    }

    //Create view holder for the recycler view
    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for this view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //Method that binds the elements of the view holder
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        //Format the speed to a 2 decimal number
        DecimalFormat df = new DecimalFormat("#.##");
        String speedformated = df.format(speedRecords.get(position).getSpeed());
        //Format the date
        String str = String.valueOf(speedRecords.get(position).getDatetime());
        Date date;
        String formattedDate = "";
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
            formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Set the text for each element of the view holder
        holder.textSpeed.setText("Ταχύτητα: " + speedformated);
        holder.textLimit.setText("Όριο: " + String.valueOf(speedRecords.get(position).getLimit()));
        holder.textLongitude.setText("X: " + String.valueOf(speedRecords.get(position).getLongitude()));
        holder.textLatitude.setText("Y: " + String.valueOf(speedRecords.get(position).getLatitude()));
        holder.textDatetime.setText(formattedDate);
        //Add click listener on each card view in order to open the map
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Opens new intent
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                //Opens with google maps
                intent.setPackage("com.google.android.apps.maps");
                //Set the data
                intent.setData(Uri.parse("https://www.google.com/maps/search/?api=1&query="+
                        speedRecords.get(position).getLatitude() +","+speedRecords.get(position).getLongitude()));
                view.getContext().startActivity(intent);
            }
        });
    }

    //Method that returns the number of the items which the recycler view contains
    @Override
    public int getItemCount() {
        return speedRecords.size();
    }

    //Set the views for the view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textSpeed, textLimit, textLongitude, textLatitude, textDatetime;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textSpeed = itemView.findViewById(R.id.speed);
            textLimit = itemView.findViewById(R.id.limit);
            textLongitude = itemView.findViewById(R.id.longitude);
            textLatitude = itemView.findViewById(R.id.latitude);
            textDatetime = itemView.findViewById(R.id.datetime);
            cardView = itemView.findViewById(R.id.cardView2);
        }
    }
}
