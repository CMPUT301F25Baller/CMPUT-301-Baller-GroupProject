package com.example.ballerevents;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityAdminProfilesBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for the Administrator to browse and manage user profiles.
 * Allows the admin to view profile details and permanently delete users from the system.
 */
public class AdminProfilesActivity extends AppCompatActivity {

    private static final String TAG = "AdminProfilesActivity";
    private ActivityAdminProfilesBinding binding;
    private FirebaseFirestore db;
    private AdminProfilesAdapter adapter;
    private final List<UserProfile> data = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProfilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.tvTitle.setText("Profiles");
        binding.btnBack.setOnClickListener(v -> finish());

        setupRecycler();
        loadAllProfiles();
    }

    /**
     * Initializes the RecyclerView and Adapter with click and delete listeners.
     */
    private void setupRecycler() {
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminProfilesAdapter(new AdminProfilesAdapter.OnProfileActionListener() {
            @Override
            public void onProfileClick(UserProfile profile) {
                Intent intent = new Intent(AdminProfilesActivity.this, ProfileDetailsActivity.class);
                intent.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_ID, profile.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(UserProfile profile) {
                new AlertDialog.Builder(AdminProfilesActivity.this)
                        .setTitle("Delete User?")
                        .setMessage("Delete " + profile.getName() + " permanently?")
                        .setPositiveButton("Delete", (d, w) -> deleteUser(profile.getId()))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        binding.recycler.setAdapter(adapter);
    }

    /**
     * Fetches all user profiles from Firestore and populates the list.
     */
    private void loadAllProfiles() {
        binding.progress.setVisibility(View.VISIBLE);
        db.collection("users").get()
                .addOnSuccessListener(snap -> {
                    binding.progress.setVisibility(View.GONE);
                    data.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        UserProfile p = doc.toObject(UserProfile.class);
                        if (p != null) {
                            p.setUid(doc.getId());
                            data.add(p);
                        }
                    }
                    adapter.submitList(new ArrayList<>(data));
                })
                .addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    Log.w(TAG, "Error", e);
                });
    }

    /**
     * Deletes a user document from Firestore.
     *
     * @param userId The unique identifier of the user to delete.
     */
    private void deleteUser(String userId) {
        if (userId == null) return;
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    loadAllProfiles();
                });
    }
}