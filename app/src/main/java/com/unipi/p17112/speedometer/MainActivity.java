package com.unipi.p17112.speedometer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener {

    Button limitButton, startButton, historyButton;
    FloatingActionButton helpButton, micButton;
    TextView current_limit, current_speed;
    SharedPreferences preferences;
    LocationManager locationManager;
    SQLiteDatabase database;
    MyTts myTts;
    AlertDialog.Builder alert;
    AlertDialog alertDialog;

    int limit;
    boolean start = false;
    private static final int REC_RESULT = 653;

    //This list will contain all the speedRecords while the speed is over the limit each time
    ArrayList<SpeedRecord> speedRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Find Views by ID
        limitButton = findViewById(R.id.limit_button);
        startButton = findViewById(R.id.start_button);
        historyButton = findViewById(R.id.history_button);
        helpButton = findViewById(R.id.help_button);
        micButton = findViewById(R.id.mic_button);
        current_limit = findViewById(R.id.textView3);
        current_speed = findViewById(R.id.textView2);
        //Set Button Clicks
        helpButton.setOnClickListener((view) -> openHelp());
        limitButton.setOnClickListener((view) -> openModal());
        historyButton.setOnClickListener((view) -> openHistory());
        micButton.setOnClickListener((view) -> recognise());
        //Create or Open the database with the table
        database = openOrCreateDatabase("SpeedDB", Context.MODE_PRIVATE,null);
        database.execSQL("CREATE TABLE IF NOT EXISTS Speed(latitude FLOAT, longitude FLOAT, timestamp TEXT, speed FLOAT, speed_limit INT)");
        //Get the limit from the shared preferences or set the default value
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        limit = preferences.getInt("Limit", -1);
        //If the limit is 0 means that the user haven't change it yet so open the modal to insert the value in other case show the current limit
        if(limit==-1){
            openModal();
            current_limit.setText("Δεν έχει οριστεί όριο ταχύτητας!");
        }
        else{
            current_limit.setText(new StringBuilder().append("Τρέχον όριο ταχύτητας: ").append(String.valueOf(limit)).append(" χλμ/ω").toString());
        }

        //Create the location manager for the gps
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Create the alert dialog which will be shown when over-speeding
        alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("ΠΡΟΣΟΧΗ!")
                .setMessage("ΥΠΕΡΒΑΣΗ ΟΡΙΟΥ ΤΑΧΥΤΗΤΑΣ!")
                .setCancelable(false)
                .setIcon(R.drawable.ic_baseline_warning_24);
        alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        //Create the text to speech
        myTts = new MyTts(this);
    }

    //Method that opens the help modal
    public void openHelp(){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.help, null);
        alert.setView(view);
        Button btn_ok = view.findViewById(R.id.btn_ok2);
        AlertDialog alertDialog = alert.create();
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    //Method that opens the modal for the limit change
    public void openModal() {
        //If the speed meter has started show message to stop the meter in order to continue
        if(start){
            Toast.makeText(this, "Τερματίστε την καταγραφή για να συνεχίσετε!", Toast.LENGTH_LONG).show();
        }
        //Else open the modal, save the user input to the shared preferences and limit variable and change the text of the current speed
        else{
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            View view = getLayoutInflater().inflate(R.layout.modal, null);
            EditText limit_edit = view.findViewById(R.id.limit_edit);
            Button btn_ok = view.findViewById(R.id.btn_ok);
            alert.setView(view);
            AlertDialog alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            //If it's the first time opening the app
            if(limit==-1){
                limit_edit.setText("0");
            }
            else{
                limit_edit.setText(String.valueOf(limit));
            }
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("Limit", Integer.parseInt(limit_edit.getText().toString()));
                    editor.apply();
                    limit = preferences.getInt("Limit", 0);
                    current_limit.setText(new StringBuilder().append("Τρέχον όριο ταχύτητας: ").append(String.valueOf(limit)).append(" χλμ/ω").toString());
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    //Method that opens the history activity
    public void openHistory(){
        //If the speed meter has started show message to stop the meter in order to continue
        if (start){
            Toast.makeText(this, "Τερματίστε την καταγραφή για να συνεχίσετε!", Toast.LENGTH_LONG).show();
        }
        //Else open the activity
        else {
            startActivity(new Intent(getApplicationContext(), MainActivity2.class));
        }
    }

    //Method that checks if the permission for the location is granted and then starts the speed meter
    public void openGPS(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 234);
            return;
        }
        start();
    }

    //Method that starts the speed meter
    public void start(){
        //If the speed meter has not started ask for gps signal, change the button text and icon and change the boolean variable
        if(!start){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            start = true;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            startButton.setText("ΤΕΡΜΑΤΙΣΜΟΣ");
            current_speed.setText("ΑΝΑΜΟΝΗ ΓΙΑ GPS");
            Drawable img = startButton.getContext().getResources().getDrawable( R.drawable.ic_baseline_cancel_40 );
            startButton.setCompoundDrawablesWithIntrinsicBounds( null, img, null, null);
        }
        //Else stop the meter, stop asking for gps signal, change the button text and icon and change the boolean variable
        else{
            locationManager.removeUpdates(this);
            start = false;
            startButton.setText("ΕΚΚΙΝΗΣΗ");
            Drawable img = startButton.getContext().getResources().getDrawable( R.drawable.ic_baseline_speed_40 );
            startButton.setCompoundDrawablesWithIntrinsicBounds( null, img, null, null);
            current_speed.setTextSize(40);
            current_speed.setText("ΑΝΑΜΟΝΗ ΓΙΑ ΕΚΚΙΝΗΣΗ");
        }
    }

    //Method for speech recognition, opens the prompt for the user to speak waiting for a result
    public void recognise(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Παρακαλώ μιλήστε");
        startActivityForResult(intent,REC_RESULT);
    }

    //Method for the result of the speed recognition
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REC_RESULT && resultCode==RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //If the speed meter has not started
            if(!start){
                //If the sentence of the user contains the word "εκκίνηση", the user hears the response and the meter starts
                if(matches.get(0).contains("εκκίνηση")){
                    myTts.speak("Εκκίνηση καταγραφής");
                    start();
                }
                //If the sentence of the user contains the word "όριο", the user hears the response and the modal opens
                else if(matches.get(0).contains("όριο")){
                    myTts.speak("Ορίστε το όριο ταχύτητας");
                    openModal();
                }
                //If the sentence of the user contains the word "ιστορικό", the user hears the response and the history activity opens
                else if(matches.get(0).contains("ιστορικό")){
                    myTts.speak("Ιστορικό παραβάσεων");
                    openHistory();
                }
                //In any other case the user hears and sees the response for a wrong voice command
                else {
                    myTts.speak("Λανθασμένη εντολή");
                    Toast.makeText(this, "Λανθασμένη εντολή!", Toast.LENGTH_LONG).show();
                }
            }
            //If the speed meter has started
            else{
                //If the sentence of the user contains the word "τερματισμός", the user hears the response and the meter stops
                if(matches.get(0).contains("τερματισμός")){
                    myTts.speak("Τερματισμός καταγραφής");
                    start();
                }
                //If the sentence of the user contains the word "όριο" or "ιστορικό", the user hears and sees the response to stop the meter in order to continue
                else if (matches.get(0).contains("όριο") || matches.get(0).contains("ιστορικό")){
                    myTts.speak("Τερματίστε την καταγραφή για να συνεχίσετε");
                    Toast.makeText(this, "Τερματίστε την καταγραφή για να συνεχίσετε!", Toast.LENGTH_LONG).show();
                }
                //In any other case the user hears and see the response for a wrong voice command
                else {
                    myTts.speak("Λανθασμένη εντολή");
                    Toast.makeText(this, "Λανθασμένη εντολή!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //Method that runs after the prompt for the permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean gpsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        //If gps permission was granted then run the code without the need of clicking the button again
        if (gpsAccepted) {
            start();
        }
        //Else show toast with the permission denial
        else{
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
        }
    }

    //Method that run when we get a new gps location
    @Override
    public void onLocationChanged(@NonNull Location location) {
        //Set the current speed to the text view after conversion to km/h
        float speed = (location.getSpeed() * 3600)/1000;
        current_speed.setTextSize(100);
        current_speed.setText(String.valueOf(Math.round(speed)));
        //Get the coordinates
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        //Get the timestamp and format like this for the database
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        //If the current speed is over the limit
        if(speed>limit){
            //Show the alert that was created in onCreate method
            alertDialog.show();
            //Change the background and text color
            getWindow().getDecorView().setBackgroundColor(Color.RED);
            current_speed.setTextColor(Color.WHITE);
            current_limit.setTextColor(Color.WHITE);
            //Create a new speedRecord instance and add it to the list
            SpeedRecord speedRecord = new SpeedRecord(longitude,latitude,speed,limit,currentDateTime);
            speedRecords.add(speedRecord);
            //User hears the message for over-speeding
            myTts.speak("Υπέρβαση ορίου ταχύτητας!");
        }
        //If the current speed is under the limit
        else{
            //If the list not empty (means that is the first time tha speed goes under the limit after over-speeding)
            if(!speedRecords.isEmpty()){
                //Stop the audio
                myTts.stop();
                //Close the alert message
                alertDialog.dismiss();
                //Check if night or day mode is on and change the background accordingly, then change the text color
                int dark = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                if(dark == Configuration.UI_MODE_NIGHT_YES){
                    getWindow().getDecorView().setBackgroundColor(Color.parseColor("#121212"));
                }
                else{
                    getWindow().getDecorView().setBackgroundColor(Color.WHITE);
                }
                current_speed.setTextColor(Color.GRAY);
                current_limit.setTextColor(Color.GRAY);
                //Get the speedRecord instance with the highest speed and clear the list
                SpeedRecord max_speed = Collections.max(speedRecords, Comparator.comparing(s -> s.getSpeed()));
                speedRecords.clear();
                //Save the speedRecord to the database
                database.execSQL("INSERT INTO Speed VALUES('"+max_speed.getLatitude()+"','"+max_speed.getLongitude()+"','"+
                        max_speed.getDatetime() +"','"+max_speed.getSpeed()+"','"+max_speed.getLimit()+"')");
            }
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}