package com.unipi.p17112.speedometer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter extends FragmentStateAdapter {
    public PagerAdapter(@NonNull FragmentActivity fragmentActivity){
        super(fragmentActivity);
    }

    //Method that creates the specific Fragments for each page
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new Fragment7Days();
            case 1:
                return new FragmentAll();
            default:
                return null;
        }
    }

    //Method that returns the number of the pages
    @Override
    public int getItemCount() {
        return 2;
    }
}
