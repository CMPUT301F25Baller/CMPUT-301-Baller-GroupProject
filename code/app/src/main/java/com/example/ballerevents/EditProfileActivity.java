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
 * <li>Opting out of Notifications (US 01.04.03).</li>
 * <li>Uploading or changing the profile picture.</li>
 * <li>Deleting the account permanently.</li>
 * </ul>
 */
public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private String newProfileImageUriString;
    private String existingProfileImageUrl;

    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    newProfileImageUriString = uri.toString();
                    Glide.with(this).load(uri).circleCrop().into(binding.ivProfileImage);
                }
            });

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
     * Also retrieves the current notification preference.
     */
    private void loadUserData() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile user = documentSnapshot.toObject(UserProfile.class);
                        if (user != null) {
                            binding.etName.setText(user.getName());
                            binding.etAboutMe.setText(user.getAboutMe());

                            if (user.getInterests() != null && !user.getInterests().isEmpty()) {
                                String interestsJoined = TextUtils.join(", ", user.getInterests());
                                binding.etInterests.setText(interestsJoined);
                            }

                            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                                existingProfileImageUrl = user.getProfilePictureUrl();
                                Glide.with(this).load(user.getProfilePictureUrl())
                                        .placeholder(R.drawable.placeholder_avatar1)
                                        .circleCrop()
                                        .into(binding.ivProfileImage);
                                newProfileImageUriString = user.getProfilePictureUrl();
                            }

                            Boolean notifEnabled = documentSnapshot.getBoolean("notificationsEnabled");
                            if (notifEnabled != null) {
                                binding.switchNotifications.setChecked(notifEnabled);
                            } else {
                                binding.switchNotifications.setChecked(true);
                            }
                        }
                    }
                });
    }

    /**
     * Validates input fields and updates the user's profile in Firestore.
     * Uploads new profile image to Firebase Storage if selected.
     */
    private void saveProfile() {
        String newName = binding.etName.getText().toString().trim();
        String newAboutMe = binding.etAboutMe.getText().toString().trim();
        String newInterestsString = binding.etInterests.getText().toString().trim();
        boolean notificationsEnabled = binding.switchNotifications.isChecked();

        if (TextUtils.isEmpty(newName)) {
            binding.etName.setError("Name required");
            return;
        }

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Saving...");

        DocumentReference userRef = db.collection("users").document(currentUserId);

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
        updates.put("notificationsEnabled", notificationsEnabled);

        if (newProfileImageUriString != null && !newProfileImageUriString.startsWith("http")) {
            Uri imageUri = Uri.parse(newProfileImageUriString);

            ImageUploadHelper.uploadProfileImage(imageUri, new ImageUploadHelper.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    updates.put("profilePictureUrl", downloadUrl);

                    if (existingProfileImageUrl != null &&
                            !existingProfileImageUrl.isEmpty() &&
                            !existingProfileImageUrl.equals(downloadUrl)) {
                        ImageUploadHelper.deleteImage(existingProfileImageUrl, new ImageUploadHelper.DeleteCallback() {
                            @Override
                            public void onSuccess() { }

                            @Override
                            public void onFailure(Exception e) {
                                android.util.Log.w("EditProfile", "Failed to delete old image", e);
                            }
                        });
                    }

                    saveToFirestore(userRef, updates);
                }

                @Override
                public void onFailure(Exception e) {
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Save Profile");
                    Toast.makeText(EditProfileActivity.this,
                            "Failed to upload profile image: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            if (newProfileImageUriString != null) {
                updates.put("profilePictureUrl", newProfileImageUriString);
            }
            saveToFirestore(userRef, updates);
        }
    }

    /**
     * Helper method to commit the update map to Firestore.
     *
     * @param userRef Reference to the user document.
     * @param updates Map of fields to update.
     */
    private void saveToFirestore(DocumentReference userRef, Map<String, Object> updates) {
        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText("Save Profile");
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays a confirmation dialog before deleting the user account.
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
     * Deletes the user profile image, Firestore document, and Firebase Authentication account.
     */
    private void performAccountDeletion() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        if (existingProfileImageUrl != null && !existingProfileImageUrl.isEmpty()) {
            ImageUploadHelper.deleteImage(existingProfileImageUrl, new ImageUploadHelper.DeleteCallback() {
                @Override
                public void onSuccess() { }

                @Override
                public void onFailure(Exception e) {
                    android.util.Log.w("EditProfile", "Failed to delete profile image during account deletion", e);
                }
            });
        }

        db.collection("users").document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
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
     * Redirects the user to the login screen after account deletion.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}