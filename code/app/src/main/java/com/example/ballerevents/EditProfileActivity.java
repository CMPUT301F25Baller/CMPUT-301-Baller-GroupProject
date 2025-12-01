package com.example.ballerevents;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Activity that allows a user to edit their profile information.
 *
 * <p>Features include:</p>
 * <ul>
 * <li>Updating Name, "About Me" bio, and Interests.</li>
 * <li>Uploading or changing the profile picture using the system image picker.</li>
 * <li>Deleting the account permanently (Firestore data + Auth).</li>
 * </ul>
 */
public class EditProfileActivity extends AppCompatActivity {

    /** View binding for the activity layout. */
    private ActivityEditProfileBinding binding;

    /** Firestore instance for database operations. */
    private FirebaseFirestore db;

    /** FirebaseAuth instance for user management. */
    private FirebaseAuth auth;

    /** The UID of the currently logged-in user. */
    private String currentUserId;

    /** Stores the URI string of a newly selected profile image, if any. */
    private String newProfileImageUriString;

    /**
     * Launcher for the image picker activity result.
     * When an image is selected, it loads it into the ImageView using Glide.
     */
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    newProfileImageUriString = uri.toString();
                    Glide.with(this).load(uri).circleCrop().into(binding.ivProfileImage);
                }
            });

    /**
     * Called when the activity is first created.
     * Initializes Firebase, sets up button listeners, and loads existing user data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in onSaveInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();

        // Handle Custom Back Button
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }

        loadUserData();

        binding.ivProfileImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.btnDeleteProfile.setOnClickListener(v -> confirmDeleteAccount());
    }

    /**
     * Fetches the current user's data from Firestore and populates the UI fields.
     */
    private void loadUserData() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile user = documentSnapshot.toObject(UserProfile.class);
                        if (user != null) {
                            binding.etName.setText(user.getName());
                            binding.etAboutMe.setText(user.getAboutMe());

                            // Join interests list into a comma-separated string
                            if (user.getInterests() != null && !user.getInterests().isEmpty()) {
                                String interestsJoined = TextUtils.join(", ", user.getInterests());
                                binding.etInterests.setText(interestsJoined);
                            }

                            // Load profile picture
                            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                                Glide.with(this).load(user.getProfilePictureUrl())
                                        .placeholder(R.drawable.placeholder_avatar1)
                                        .circleCrop()
                                        .into(binding.ivProfileImage);
                                newProfileImageUriString = user.getProfilePictureUrl();
                            }
                        }
                    }
                });
    }

    /**
     * Validates input fields and updates the user's profile in Firestore.
     * Parses the comma-separated interests string into a List.
     */
    private void saveProfile() {
        String newName = binding.etName.getText().toString().trim();
        String newAboutMe = binding.etAboutMe.getText().toString().trim();
        String newInterestsString = binding.etInterests.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            binding.etName.setError("Name required");
            return;
        }

        DocumentReference userRef = db.collection("users").document(currentUserId);

        // Parse interests string to List
        List<String> newInterests;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newInterests = Arrays.stream(newInterestsString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            newInterests = new java.util.ArrayList<>();
            for (String s : newInterestsString.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) newInterests.add(trimmed);
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("aboutMe", newAboutMe);
        updates.put("interests", newInterests);

        if (newProfileImageUriString != null) {
            updates.put("profilePictureUrl", newProfileImageUriString);
        }

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving profile.", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Displays an alert dialog asking the user to confirm account deletion.
     */
    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is permanent and cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> performAccountDeletion())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the user's Firestore document and their Firebase Authentication record.
     * If successful, redirects to the Login screen.
     */
    private void performAccountDeletion() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // 1. Delete Firestore Document
        db.collection("users").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. Delete Authentication User
                    user.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Account deleted.", Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete auth account. Please re-login and try again.", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete user data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigates back to the LoginActivity and clears the task stack.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}