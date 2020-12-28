package com.unipi.p17112.speedometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Set the title and the back button on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ιστορικό Παραβάσεων");

        //Set the viewpager with the adapter which will contain the recycler view with the cards
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        viewPager2.setAdapter(new PagerAdapter(this));

        //Set the tab layout which will change the viewpager (weekly violations and all violations)
        TabLayout tabLayout = findViewById(R.id.tabBar);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:{
                        tab.setText("ΤΕΛΕΥΤΑΙΕΣ 7 ΜΕΡΕΣ");
                        break;
                    }
                    case 1:{
                        tab.setText("ΟΛΕΣ");
                        break;
                    }
                }
            }
        });
        tabLayoutMediator.attach();
    }
}