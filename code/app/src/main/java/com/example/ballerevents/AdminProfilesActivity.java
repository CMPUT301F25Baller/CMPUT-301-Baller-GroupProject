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
 * Displays all user profiles for the Admin role.
 *
 * <p>This screen uses Firestore to load all documents from the "users"
 * collection and displays them using {@link AdminProfilesAdapter}.
 *
 * <p>Admin actions:
 * <ul>
 *     <li>View the full list of users</li>
 *     <li>Delete a user document permanently</li>
 *     <li>(Future) Open a full profile detail screen for moderation</li>
 * </ul>
 *
 * <p>The UI provides:
 * <ul>
 *     <li>Toolbar with back navigation</li>
 *     <li>A RecyclerView showing each user profile</li>
 *     <li>A progress indicator shown during Firestore loading</li>
 * </ul>
 *
 * <p>This Activity is strictly for admin moderation and does not alter
 * the user authentication state or modify events.
 */
public class AdminProfilesActivity extends AppCompatActivity {

    private static final String TAG = "AdminProfilesActivity";

    private ActivityAdminProfilesBinding binding;
    private FirebaseFirestore db;

    private AdminProfilesAdapter adapter;
    private final List<UserProfile> data = new ArrayList<>();

    /**
     * Result launcher to handle future navigation to a profile details screen.
     * Currently unused since the profile detail screen is not implemented.
     */
    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String deletedId = result.getData().getStringExtra("EXTRA_PROFILE_ID");
                            if (deletedId != null) {
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

        setupToolbar();
        setupRecycler();
        loadAllProfiles();
    }

    /**
     * Configures the top AppBar for back navigation and screen title.
     */
    private void setupToolbar() {
        binding.topAppBar.setTitle("Profiles");
        binding.topAppBar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Initializes the RecyclerView and its adapter for listing user profiles.
     */
    private void setupRecycler() {
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProfilesAdapter(this::openDetails);
        binding.recycler.setAdapter(adapter);
    }

    /**
     * Loads all user documents from the Firestore "users" collection.
     * Displays a loading indicator while data is fetched.
     */
    private void loadAllProfiles() {
        binding.progress.setVisibility(View.VISIBLE);

        db.collection("users")
                .get()
                .addOnSuccessListener(snapshot -> {
                    binding.progress.setVisibility(View.GONE);
                    data.clear();
                    data.addAll(snapshot.toObjects(UserProfile.class));
                    adapter.submitList(new ArrayList<>(data));
                })
                .addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    Log.w(TAG, "Error loading all profiles", e);
                    Toast.makeText(this, "Error loading profiles", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Called when the admin selects a profile from the list.
     * Currently shows a delete confirmation dialog directly.
     *
     * @param profile the profile selected by the admin
     */
    private void openDetails(UserProfile profile) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User?")
                .setMessage("Delete user " + profile.getName() + "? This is permanent.")
                .setPositiveButton("Delete", (d, w) -> deleteUserFromFirestore(profile.getId()))
                .setNegativeButton("Cancel", null)
                .show();

        // Future Feature:
        // Launch a full detail screen where the admin can moderate more fields.
        /*
        Intent i = new Intent(this, ProfileDetailsActivity.class);
        i.putExtra("EXTRA_PROFILE_ID", profile.getId());
        ...
        detailLauncher.launch(i);
        */
    }

    /**
     * Deletes the Firestore document for the given user ID.
     * After deletion, the profile list is reloaded.
     *
     * @param userId Firestore user document ID
     */
    private void deleteUserFromFirestore(String userId) {
        if (userId == null) return;

        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    loadAllProfiles();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting user", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error deleting user", e);
                });
    }
}
