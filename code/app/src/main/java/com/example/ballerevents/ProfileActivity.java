package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ballerevents.databinding.ActivityProfileBinding;
import com.google.android.material.chip.Chip;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private NearEventAdapter joinedEventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load data every time the activity is resumed
        // This ensures data is fresh after returning from EditProfileActivity
        loadProfileData();
        loadJoinedEvents();
    }

    private void setupListeners() {
        binding.btnBackProfile.setOnClickListener(v -> finish());
        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });
    }

    private void loadProfileData() {
        UserProfile userProfile = EventRepository.getUserProfile(EventRepository.MOCK_USER_ID);

        if (userProfile == null) return;

        binding.ivProfilePicture.setImageResource(userProfile.getProfilePictureResId());
        binding.tvProfileName.setText(userProfile.getName());
        binding.tvFollowingCount.setText(String.valueOf(userProfile.getFollowingCount()));
        binding.tvFollowerCount.setText(String.valueOf(userProfile.getFollowerCount()));
        binding.tvAboutMe.setText(userProfile.getAboutMe());

        // Populate Interests
        binding.chipGroupInterests.removeAllViews(); // Clear old chips
        List<String> interests = userProfile.getInterests();
        if (interests != null) {
            for (String interest : interests) {
                Chip chip = new Chip(this);
                chip.setText(interest);
                // You can style chips here (e.g., setChipBackgroundColor)
                binding.chipGroupInterests.addView(chip);
            }
        }
    }

    private void setupRecyclerView() {
        joinedEventsAdapter = new NearEventAdapter(event -> {
            // Go to event details when a joined event is clicked
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvJoinedEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJoinedEvents.setAdapter(joinedEventsAdapter);
    }

    private void loadJoinedEvents() {
        List<Event> joinedEvents = EventRepository.getAppliedEvents(EventRepository.MOCK_USER_ID);
        joinedEventsAdapter.submitList(joinedEvents);
    }
}