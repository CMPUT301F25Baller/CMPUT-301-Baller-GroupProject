package com.example.ballerevents;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter for supplying organizer-related fragments to the ViewPager2
 * in {@link OrganizerActivity}.
 *
 * <p>The pager displays three fragments:</p>
 * <ul>
 *     <li><b>About</b> – {@link OrganizerAboutFragment}</li>
 *     <li><b>Event</b> – {@link OrganizerEventFragment}</li>
 *     <li><b>Following</b> – {@link OrganizerFollowingFragment}</li>
 * </ul>
 *
 * <p>
 * Fragments do not receive arguments through this adapter. Each fragment
 * independently queries FirebaseAuth to obtain the current organizer ID,
 * keeping this adapter simple and minimally coupled.
 * </p>
 */
public class OrganizerPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructs the adapter used by the organizer ViewPager.
     *
     * @param fa The hosting {@link FragmentActivity}.
     */
    public OrganizerPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    /**
     * Creates and returns a new fragment instance based on the requested tab index.
     *
     * @param position The index of the selected page (0–2).
     * @return The fragment corresponding to the given position.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OrganizerAboutFragment();
            case 1:
                return new OrganizerEventFragment();
            default:
                return new OrganizerFollowingFragment();
        }
    }

    /**
     * Returns the number of pages managed by this adapter.
     *
     * @return Always 3 ("About", "Event", "Following").
     */
    @Override
    public int getItemCount() {
        return 3;
    }
}
