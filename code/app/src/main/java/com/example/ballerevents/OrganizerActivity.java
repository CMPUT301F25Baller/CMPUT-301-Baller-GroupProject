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
 * Main activity for organizers, hosting a tab-based layout with the following
 * sections:
 * <ul>
 *     <li>About – displays profile details</li>
 *     <li>Event – lists events created by the organizer</li>
 *     <li>Following – shows followed entrants or other entities (prototype)</li>
 * </ul>
 *
 * <p>The activity loads the organizer’s profile header (name and avatar),
 * configures a {@link ViewPager2} with {@link OrganizerPagerAdapter}, and provides
 * buttons for creating events and accessing messaging features.</p>
 */
public class OrganizerActivity extends AppCompatActivity {

    /** Logging tag for debugging organizer screen behavior. */
    private static final String TAG = "OrganizerActivity";

    /** Titles for the ViewPager tabs. */
    private static final String[] TAB_TITLES = {"About", "Event", "Following"};

    /** ViewBinding reference for accessing layout views. */
    private ActivityOrganizerBinding binding;

    /** Firestore instance for retrieving organizer profile data. */
    private FirebaseFirestore db;

    /** FirebaseAuth instance for checking logged-in user. */
    private FirebaseAuth mAuth;

    /** Currently authenticated organizer's user ID. */
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ensure user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Configure toolbar navigation
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Setup ViewPager and page adapter
        binding.viewPager.setAdapter(new OrganizerPagerAdapter(this));
        binding.viewPager.setOffscreenPageLimit(2);

        // Default to middle tab (Event tab)
        binding.viewPager.setCurrentItem(1, false);

        // Connect TabLayout and ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        // Create event button
        binding.btnNewEvent.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerEventCreationActivity.class))
        );

        // Messaging button (prototype only)
        binding.btnMessage.setOnClickListener(v ->
                Toast.makeText(this, "Messaging prototype coming soon.", Toast.LENGTH_SHORT).show()
        );

        // Load organizer header info (name + profile image)
        loadOrganizerHeaderInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile header when returning from edit screen
        if (currentUserId != null) {
            loadOrganizerHeaderInfo();
        }
    }

    /**
     * Loads the organizer’s profile data from Firestore and updates the header
     * elements (organizer name and profile image).
     */
    private void loadOrganizerHeaderInfo() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);

                if (userProfile != null) {
                    // Update organizer name
                    if (binding.tvOrganizerName != null) {
                        binding.tvOrganizerName.setText(userProfile.getName());
                    }

                    // Update profile picture
                    if (binding.ivOrganizerProfile != null) {
                        Glide.with(this)
                                .load(userProfile.getProfilePictureUrl())
                                .placeholder(R.drawable.placeholder_avatar1)
                                .error(R.drawable.placeholder_avatar1)
                                .into(binding.ivOrganizerProfile);
                    }
                }
            } else {
                Toast.makeText(this, "Could not find organizer profile.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Organizer profile not found for ID: " + currentUserId);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching organizer data.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error fetching user profile", e);
        });
    }
}
