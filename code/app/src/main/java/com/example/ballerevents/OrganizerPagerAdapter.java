package com.example.ballerevents;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OrganizerPagerAdapter extends FragmentStateAdapter {

    public OrganizerPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new OrganizerAboutFragment();
            case 1: return new OrganizerEventFragment();
            default: return new OrganizerFollowingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
