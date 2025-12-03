package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityProfileDetailsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE_ID = "extra_profile_id";
    private ActivityProfileDetailsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String profileId;
    private String currentUserId;
    private HistoryAdapter historyAdapter;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        profileId = getIntent().getStringExtra(EXTRA_PROFILE_ID);

        setupHistoryRecycler();

        if (profileId != null) {
            loadFromFirestore(profileId);
            if (currentUserId != null && !currentUserId.equals(profileId)) {
                checkIfFollowing();
                setupFollowButton();
            }
        } else {
            Toast.makeText(this, "No User ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnDeleteUser.setOnClickListener(v -> confirmDelete());
    }

    private void loadFromFirestore(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        finish();
                        return;
                    }
                    UserProfile up = doc.toObject(UserProfile.class);
                    if (up != null) {
                        bindUserData(up);
                        loadEventHistory(up);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show());
    }

    private void bindUserData(UserProfile user) {
        binding.tvName.setText(user.getName() != null ? user.getName() : "User");
        binding.tvEmail.setText(user.getEmail());

        String bio = (user.getAboutMe() != null && !user.getAboutMe().isEmpty())
                ? user.getAboutMe()
                : "No bio provided.";
        binding.tvBio.setText(bio);

        // Stats
        binding.tvFollowingCount.setText(String.valueOf(user.getFollowingCount()));
        binding.tvFollowersCount.setText(String.valueOf(user.getFollowerCount()));

        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String interest : user.getInterests()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(interest);
            }
            binding.tvInterests.setText(sb.toString());
        } else {
            binding.tvInterests.setText("No interests selected.");
        }

        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.placeholder_avatar1)
                .into(binding.ivAvatar);

        // --- Role Based View ---
        if (currentUserId != null) {
            db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
                String myRole = doc.getString("role");
                if ("admin".equals(myRole)) {
                    binding.btnDeleteUser.setVisibility(View.VISIBLE);
                    binding.btnFollow.setVisibility(View.GONE);
                } else if (!currentUserId.equals(profileId)) {
                    // Not admin, and not looking at myself
                    binding.btnFollow.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    // --- FOLLOW LOGIC ---

    private void checkIfFollowing() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    UserProfile me = doc.toObject(UserProfile.class);
                    if (me != null && me.getFollowingIds() != null && me.getFollowingIds().contains(profileId)) {
                        isFollowing = true;
                    } else {
                        isFollowing = false;
                    }
                    updateFollowButtonState();
                });
    }

    private void updateFollowButtonState() {
        if (isFollowing) {
            binding.btnFollow.setText("Unfollow");
            binding.btnFollow.setBackgroundColor(0xFFE0E0E0); // Grey
            binding.btnFollow.setTextColor(0xFF000000); // Black
        } else {
            binding.btnFollow.setText("Follow");
            binding.btnFollow.setBackgroundColor(0xFF5A00FF); // Purple
            binding.btnFollow.setTextColor(0xFFFFFFFF); // White
        }
    }

    private void setupFollowButton() {
        binding.btnFollow.setOnClickListener(v -> {
            if (isFollowing) unfollowUser();
            else followUser();
        });
    }

    private void followUser() {
        // Add to my following
        db.collection("users").document(currentUserId)
                .update("followingIds", FieldValue.arrayUnion(profileId));

        // Add to target's followers
        db.collection("users").document(profileId)
                .update("followerIds", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener(a -> {
                    isFollowing = true;
                    updateFollowButtonState();
                    sendFollowNotification();
                    Toast.makeText(this, "Followed!", Toast.LENGTH_SHORT).show();
                });
    }

    private void unfollowUser() {
        db.collection("users").document(currentUserId)
                .update("followingIds", FieldValue.arrayRemove(profileId));

        db.collection("users").document(profileId)
                .update("followerIds", FieldValue.arrayRemove(currentUserId))
                .addOnSuccessListener(a -> {
                    isFollowing = false;
                    updateFollowButtonState();
                    Toast.makeText(this, "Unfollowed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendFollowNotification() {
        db.collection("users").document(currentUserId).get().addOnSuccessListener(doc -> {
            String myName = doc.getString("name");
            if (myName == null) myName = "Someone";

            Map<String, Object> notif = new HashMap<>();
            notif.put("title", "New Follower");
            notif.put("message", myName + " started following you!");
            notif.put("type", "new_follower");
            notif.put("senderId", currentUserId); // For follow back
            notif.put("timestamp", new Date());
            notif.put("read", false);

            db.collection("users").document(profileId)
                    .collection("notifications").add(notif);
        });
    }

    // --- EVENT HISTORY ---

    private void setupHistoryRecycler() {
        binding.rvEventHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter();
        binding.rvEventHistory.setAdapter(historyAdapter);
    }

    private void loadEventHistory(UserProfile user) {
        Set<String> allEventIds = new HashSet<>();
        if (user.getAppliedEventIds() != null) allEventIds.addAll(user.getAppliedEventIds());
        if (user.getInvitedEventIds() != null) allEventIds.addAll(user.getInvitedEventIds());
        if (user.getJoinedEventIds() != null) allEventIds.addAll(user.getJoinedEventIds());

        if (allEventIds.isEmpty()) {
            binding.tvNoHistory.setVisibility(View.VISIBLE);
            return;
        }

        binding.progressHistory.setVisibility(View.VISIBLE);
        List<String> idList = new ArrayList<>(allEventIds);
        List<String> queryIds = idList.subList(0, Math.min(idList.size(), 10));

        db.collection("events")
                .whereIn(FieldPath.documentId(), queryIds)
                .get()
                .addOnSuccessListener(snap -> {
                    binding.progressHistory.setVisibility(View.GONE);
                    List<HistoryItem> items = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            String status = determineStatus(user, doc.getId());
                            items.add(new HistoryItem(e.getTitle(), status, e.getDate()));
                        }
                    }
                    historyAdapter.submitList(items);
                    if (items.isEmpty()) binding.tvNoHistory.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> binding.progressHistory.setVisibility(View.GONE));
    }

    private String determineStatus(UserProfile user, String eventId) {
        if (user.getJoinedEventIds() != null && user.getJoinedEventIds().contains(eventId)) return "Going ✅";
        if (user.getInvitedEventIds() != null && user.getInvitedEventIds().contains(eventId)) return "Invited 📩";
        if (user.getAppliedEventIds() != null && user.getAppliedEventIds().contains(eventId)) return "Waitlisted ⏳";
        return "History";
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete User?")
                .setMessage("Permanently delete this user?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection("users").document(profileId).delete()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Custom Adapter for History
    private static class HistoryItem {
        String title, status, date;
        HistoryItem(String t, String s, String d) { title = t; status = s; date = d; }
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        private List<HistoryItem> list = new ArrayList<>();

        void submitList(List<HistoryItem> newList) {
            list = newList;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Using the custom layout you provided
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            HistoryItem item = list.get(position);
            h.title.setText(item.title);
            h.date.setText(item.date);
            h.status.setText(item.status);

            if (item.status.contains("Going")) {
                h.status.setTextColor(0xFF4CAF50);
            } else if (item.status.contains("Invited")) {
                h.status.setTextColor(0xFF5A00FF);
            } else {
                h.status.setTextColor(0xFF757575);
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, date, status;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tvEventTitle);
                date = v.findViewById(R.id.tvEventDate);
                status = v.findViewById(R.id.tvStatusBadge);
            }
        }
    }
}