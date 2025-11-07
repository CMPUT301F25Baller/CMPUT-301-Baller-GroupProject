package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

// Import ViewBinding and required Firebase/Glide classes
import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityOrganizerBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerActivity";
    private static final String[] TAB_TITLES = {"About", "Event", "Following"};

    // Use ViewBinding
    private ActivityOrganizerBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate using ViewBinding
        binding = ActivityOrganizerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Go back to login if no user
            return;
        }
        currentUserId = mAuth.getCurrentUser().getUid();

        // Setup Toolbar
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Setup ViewPager
        binding.viewPager.setAdapter(new OrganizerPagerAdapter(this));
        binding.viewPager.setOffscreenPageLimit(2);

        // Default to Event tab (index 1)
        binding.viewPager.setCurrentItem(1, false);

        // Link ViewPager and TabLayout
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();

        // Setup button listeners using binding
        binding.btnNewEvent.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerEventCreationActivity.class))
        );

        binding.btnMessage.setOnClickListener(v ->
                Toast.makeText(this, "Messaging prototype coming soon.", Toast.LENGTH_SHORT).show()
        );

        // Load organizer's header info
        loadOrganizerHeaderInfo();
    }

    /**
     * Fetches the current organizer's profile to display their name and picture.
     */
    private void loadOrganizerHeaderInfo() {
        DocumentReference userRef = db.collection("users").document(currentUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    // Set organizer name (assuming ID tvOrganizerName exists in the layout)
                    if (binding.tvOrganizerName != null) {
                        binding.tvOrganizerName.setText(userProfile.getName());
                    }

                    // Load profile picture (assuming ID ivOrganizerProfile exists)
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