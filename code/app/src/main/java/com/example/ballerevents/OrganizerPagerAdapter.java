package com.example.ballerevents;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Pager adapter used by {@link OrganizerActivity} to manage the three main
 * organizer-related fragments displayed inside a ViewPager2:
 *
 * <ul>
 *     <li><b>About</b> – {@link OrganizerAboutFragment}</li>
 *     <li><b>Event</b> – {@link OrganizerEventFragment}</li>
 *     <li><b>Following</b> – {@link OrganizerFollowingFragment}</li>
 * </ul>
 *
 * <p>
 * The adapter does not pass arguments to fragments because they independently
 * obtain the current organizer ID from FirebaseAuth. This keeps the pager
 * simple and avoids unnecessary coupling.
 */
public class OrganizerPagerAdapter extends FragmentStateAdapter {

    // The organizerId is no longer needed here,
    // as the fragments will get it from FirebaseAuth.

    /**
     * Creates a new adapter for the organizer ViewPager.
     *
     * @param fa the hosting {@link FragmentActivity}
     */
    public OrganizerPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    /**
     * Creates a new fragment instance based on the selected tab index.
     *
     * @param position index of the requested fragment
     * @return the corresponding Fragment instance
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // We no longer call newInstance(organizerId)
        // Just create new instances.
        switch (position) {
            case 0: return new OrganizerAboutFragment();
            case 1: return new OrganizerEventFragment();
            default: return new OrganizerFollowingFragment();
        }
    }

    /**
     * @return the number of pages hosted by this adapter (always 3)
     */
    @Override
    public int getItemCount() {
        return 3; // "About", "Event", "Following"
    }
}