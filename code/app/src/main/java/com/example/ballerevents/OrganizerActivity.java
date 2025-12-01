package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityOrganizerBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Main activity for users with the Organizer role.
 *
 * <p>This activity displays the organizer's profile header (Name, Bio, Stats)
 * designed to be visually consistent with the Entrant profile. It hosts a
 * {@link ViewPager2} with three tabs:</p>
 * <ul>
 * <li><b>About</b>: Detailed profile information.</li>
 * <li><b>Event</b>: List of events managed by the organizer.</li>
 * <li><b>Following</b>: Lists of followers and following users.</li>
 * </ul>
 */
public class OrganizerActivity extends AppCompatActivity {

    /** Tag for logging errors and debug info. */
    private static final String TAG = "OrganizerActivity";

    /** Titles for the tabs displayed in the TabLayout. */
    private static final String[] TAB_TITLES = {"About", "Event", "Following"};

    /** View binding for the activity layout. */
    private ActivityOrganizerBinding binding;

    /** Firestore instance for fetching profile data. */
    private FirebaseFirestore db;

    /** FirebaseAuth instance for authentication checks. */
    private FirebaseAuth mAuth;

    /** The UID of the current organizer. */
    private String currentUserId;

    /**
     * Called when the activity is first created.
     * Initializes views, sets up the ViewPager/TabLayout, and loads profile data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Handle Custom Back Button
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }

        // Setup ViewPager
        binding.viewPager.setAdapter(new OrganizerPagerAdapter(this));
        binding.viewPager.setOffscreenPageLimit(2);
        binding.viewPager.setCurrentItem(1, false); // Default to Event tab

        // Connect TabLayout to ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        // "New Event" Button Listener
        binding.btnNewEvent.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerEventCreationActivity.class))
        );

        // "Message" Button Listener (Prototype)
        binding.btnMessage.setOnClickListener(v ->
                Toast.makeText(this, "Messaging prototype coming soon.", Toast.LENGTH_SHORT).show()
        );

        loadOrganizerHeaderInfo();
    }

    /**
     * Called when the activity resumes.
     * Reloads the header info to ensure stats/bio are up-to-date if changed elsewhere.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null) {
            loadOrganizerHeaderInfo();
        }
    }

    /**
     * Fetches the organizer's profile document from Firestore.
     * Updates the header UI elements: Name, Bio (About Me), Profile Picture, and Follower/Following counts.
     */
    private void loadOrganizerHeaderInfo() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);

                if (userProfile != null) {
                    // Update Name
                    binding.tvOrganizerName.setText(userProfile.getName());

                    // Update About Me (Bio)
                    if (binding.tvOrganizerAboutMe != null) {
                        binding.tvOrganizerAboutMe.setText(userProfile.getAboutMe());
                    }

                    // Update Profile Image
                    Glide.with(this)
                            .load(userProfile.getProfilePictureUrl())
                            .placeholder(R.drawable.placeholder_avatar1)
                            .error(R.drawable.placeholder_avatar1)
                            .into(binding.ivOrganizerProfile);

                    // Update Stats
                    int following = userProfile.getFollowingIds() != null ? userProfile.getFollowingIds().size() : 0;
                    int followers = userProfile.getFollowerIds() != null ? userProfile.getFollowerIds().size() : 0;

                    if (binding.tvFollowingCount != null) {
                        binding.tvFollowingCount.setText(String.valueOf(following));
                    }
                    if (binding.tvFollowersCount != null) {
                        binding.tvFollowersCount.setText(String.valueOf(followers));
                    }
                }
            } else {
                Toast.makeText(this, "Could not find organizer profile.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching user profile", e);
        });
    }
}