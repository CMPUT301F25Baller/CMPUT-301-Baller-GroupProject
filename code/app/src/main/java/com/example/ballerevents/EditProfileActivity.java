package com.example.ballerevents;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Allows the user to edit their Profile Name, Picture, "About Me", and "Interests".
 * Fetches the current profile data on load and saves it back to
 * Firestore on button click.
 */
public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    // To store the new image URI if selected
    private String newProfileImageUriString = null;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = db.collection("users").document(currentUserId);

        setupImagePicker();
        loadCurrentData();
        setupListeners();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                newProfileImageUriString = uri.toString();
                // Show the selected image immediately
                Glide.with(this).load(uri).into(binding.ivProfileImage);
            }
        });
    }

    /**
     * Fetches the current user's profile data from Firestore to populate fields.
     */
    private void loadCurrentData() {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    // Populate Name
                    binding.etName.setText(userProfile.getName());

                    // Populate About Me
                    binding.etAboutMe.setText(userProfile.getAboutMe());

                    // Populate Profile Picture
                    if (userProfile.getProfilePictureUrl() != null && !userProfile.getProfilePictureUrl().isEmpty()) {
                        Glide.with(this)
                                .load(userProfile.getProfilePictureUrl())
                                .placeholder(R.drawable.placeholder_avatar1)
                                .error(R.drawable.placeholder_avatar1)
                                .into(binding.ivProfileImage);
                    }

                    // Populate Interests
                    if (userProfile.getInterests() != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            String interests = String.join(", ", userProfile.getInterests());
                            binding.etInterests.setText(interests);
                        } else {
                            StringBuilder interestsBuilder = new StringBuilder();
                            for (String interest : userProfile.getInterests()) {
                                if (interestsBuilder.length() > 0) {
                                    interestsBuilder.append(", ");
                                }
                                interestsBuilder.append(interest);
                            }
                            binding.etInterests.setText(interestsBuilder.toString());
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnBackEdit.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveProfileData());

        // Clicking image or text opens gallery
        binding.ivProfileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.tvChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void saveProfileData() {
        String newName = binding.etName.getText().toString().trim();
        String newAboutMe = binding.etAboutMe.getText().toString().trim();
        String newInterestsString = binding.etInterests.getText().toString();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> newInterests;
        // Convert comma-separated string back to List<String>
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newInterests = Arrays.stream(newInterestsString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            newInterests = new java.util.ArrayList<>();
            for (String s : newInterestsString.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    newInterests.add(trimmed);
                }
            }
        }

        // Build update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("aboutMe", newAboutMe);
        updates.put("interests", newInterests);

        // Only update image URL if user picked a new one
        if (newProfileImageUriString != null) {
            updates.put("profilePictureUrl", newProfileImageUriString);
            // NOTE: For a real app, you must upload this URI to Firebase Storage first
            // and save the download URL here. Currently, this saves the local device URI.
        }

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile.", Toast.LENGTH_SHORT).show();
                });
    }
}