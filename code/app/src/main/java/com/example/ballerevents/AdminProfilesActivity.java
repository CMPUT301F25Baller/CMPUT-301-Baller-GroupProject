package com.example.ballerevents;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityAdminProfilesBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/**
 * Admin list of user profiles. Loads users from Firestore into
 * {@link AdminProfilesAdapter}, supports opening a profile for actions,
 * and provides a delete flow that removes the user document and reloads the list.
 *
 * UI:
 *  - Toolbar back
 *  - RecyclerView of profiles
 *  - Progress indicator while loading
 */


public class AdminProfilesActivity extends AppCompatActivity {

    private static final String TAG = "AdminProfilesActivity";
    private ActivityAdminProfilesBinding binding;
    private FirebaseFirestore db;

    private AdminProfilesAdapter adapter;
    private final List<UserProfile> data = new ArrayList<>();

    // Launcher to handle result from a (hypothetical) ProfileDetailsActivity
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String deletedId = result.getData().getStringExtra("EXTRA_PROFILE_ID"); // Use a constant
                    if (deletedId != null) {
                        // We will delete the user and then reload from Firestore
                        deleteUserFromFirestore(deletedId);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProfilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.topAppBar.setTitle("Profiles");
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProfilesAdapter(this::openDetails);
        binding.recycler.setAdapter(adapter);

        loadAllProfiles();
    }

    private void loadAllProfiles() {
        binding.progress.setVisibility(View.VISIBLE);
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progress.setVisibility(View.GONE);
                    data.clear();
                    data.addAll(queryDocumentSnapshots.toObjects(UserProfile.class));
                    adapter.submitList(new ArrayList<>(data));
                })
                .addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    Log.w(TAG, "Error loading all profiles", e);
                    Toast.makeText(this, "Error loading profiles", Toast.LENGTH_SHORT).show();
                });
    }

    private void openDetails(UserProfile p) {
        // This launches a ProfileDetailsActivity which was not provided.
        // We can just show a delete dialog here for now.
        new AlertDialog.Builder(this)
                .setTitle("Delete User?")
                .setMessage("Delete user " + p.getName() + "? This is permanent.")
                .setPositiveButton("Delete", (d, w) -> deleteUserFromFirestore(p.getId()))
                .setNegativeButton("Cancel", null)
                .show();

        // TODO: When ProfileDetailsActivity exists, use this code:
        /*
        Intent i = new Intent(this, ProfileDetailsActivity.class);
        i.putExtra("EXTRA_PROFILE_ID", p.getId());
        i.putExtra("EXTRA_PROFILE_NAME", p.getName());
        i.putExtra("EXTRA_PROFILE_AVATAR_URL", p.getProfilePictureUrl());
        i.putExtra("EXTRA_PROFILE_BIO", p.getAboutMe());
        i.putExtra("EXTRA_PROFILE_FOLLOWERS", p.getFollowerCount());
        i.putExtra("EXTRA_PROFILE_FOLLOWING", p.getFollowingCount());
        detailLauncher.launch(i);
        */
    }

    private void deleteUserFromFirestore(String userId) {
        if (userId == null) return;

        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    loadAllProfiles(); // Reload the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting user", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error deleting user", e);
                });
    }
}