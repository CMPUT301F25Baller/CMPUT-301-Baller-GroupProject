package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityProfileBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FieldPath;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration userListener;
    private NearEventAdapter joinedEventsAdapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            setupRecyclerView();
            setupRealtimeProfileListener();
            setupButtons();
        } else {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupButtons() {
        binding.btnBackProfile.setOnClickListener(v -> finish());

        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class))
        );

        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupRecyclerView() {
        joinedEventsAdapter = new NearEventAdapter(event -> {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvJoinedEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvJoinedEvents.setAdapter(joinedEventsAdapter);
    }

    private void setupRealtimeProfileListener() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                UserProfile userProfile = snapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    updateUI(userProfile);
                    loadJoinedEvents(userProfile.getAppliedEventIds());
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }

    private void updateUI(UserProfile user) {
        binding.tvName.setText(user.getName());
        binding.tvAboutMe.setText(user.getAboutMe());
        binding.tvFollowersCount.setText(String.valueOf(user.getFollowerIds().size()));
        binding.tvFollowingCount.setText(String.valueOf(user.getFollowingIds().size()));

        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.placeholder_avatar1)
                .error(R.drawable.placeholder_avatar1)
                .into(binding.ivProfileImage);

        // Update chips
        binding.chipGroupInterests.removeAllViews();
        for (String interest : user.getInterests()) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            chip.setCheckable(false);
            binding.chipGroupInterests.addView(chip);
        }
    }

    private void loadJoinedEvents(List<String> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            joinedEventsAdapter.submitList(new ArrayList<>());
            return;
        }

        // Firestore 'in' query supports max 10 items.
        // For production apps, you'd batch this or structure data differently.
        List<String> subset = eventIds.subList(0, Math.min(eventIds.size(), 10));

        db.collection("events")
                .whereIn(FieldPath.documentId(), subset)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> joinedEvents = queryDocumentSnapshots.toObjects(Event.class);
                    joinedEventsAdapter.submitList(joinedEvents);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading joined events", e));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
        }
    }
}