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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE_ID = "extra_profile_id";
    private ActivityProfileDetailsBinding binding;
    private FirebaseFirestore db;
    private String profileId;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        profileId = getIntent().getStringExtra(EXTRA_PROFILE_ID);

        setupHistoryRecycler();

        if (profileId != null) {
            loadFromFirestore(profileId);
        } else {
            Toast.makeText(this, "No User ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.btnDeleteUser.setOnClickListener(v -> confirmDelete());
    }

    private void setupHistoryRecycler() {
        binding.rvEventHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter();
        binding.rvEventHistory.setAdapter(historyAdapter);
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

        if (user.getInterests() != null && !user.getInterests().isEmpty()) {
            // Join list with commas
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

        // Limit query to 10 for simplicity in this implementation
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

    // --- Simple Internal Adapter for History ---
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
            // Using standard simple layout for robustness
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            HistoryItem item = list.get(position);
            holder.text1.setText(item.title);
            holder.text2.setText(item.status + " • " + item.date);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
                text1.setTextColor(0xFF000000); // Black
                text2.setTextColor(0xFF5A00FF); // Purple
            }
        }
    }
}