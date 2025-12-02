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
 * and hosts a {@link ViewPager2} with three tabs:</p>
 * <ul>
 * <li><b>About</b>: Detailed profile information.</li>
 * <li><b>Event</b>: List of events managed by the organizer.</li>
 * <li><b>Following</b>: Lists of followers and following users.</li>
 * </ul>
 *
 * <p><b>Navigation Note:</b> Event-specific actions (like running a lottery) are NOT performed here.
 * Organizers must navigate to the <b>Event</b> tab, tap a specific event, and use the
 * {@link OrganizerWaitlistActivity} management screen.</p>
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";
    private static final String[] TAB_TITLES = {"About", "Event", "Following"};

    private ActivityOrganizerBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    // Stores the ID of the event selected in the Event Tab (for future features)
    private String selectedEventId;

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

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null) {
            loadOrganizerHeaderInfo();
        }
    }

    /**
     * Helper method called by fragments to set the currently selected event context.
     */
    public void setSelectedEventId(String eventId) {
        this.selectedEventId = eventId;
        // Logic can be added here if the parent activity needs to react to event selection
    }

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