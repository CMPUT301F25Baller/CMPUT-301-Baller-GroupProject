package com.example.ballerevents;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OrganizerPagerAdapter extends FragmentStateAdapter {
    private final String organizerId;
    public OrganizerPagerAdapter(@NonNull FragmentActivity fa, @NonNull String organizerId) {
        super(fa);
        this.organizerId = organizerId;
    }
    @NonNull @Override public Fragment createFragment(int position) {
        switch (position) {
            case 0: return OrganizerAboutFragment.newInstance(organizerId);
            case 1: return OrganizerEventFragment.newInstance(organizerId);
            default: return OrganizerFollowingFragment.newInstance(organizerId);
        }
    }
    @Override public int getItemCount() { return 3; }
}
