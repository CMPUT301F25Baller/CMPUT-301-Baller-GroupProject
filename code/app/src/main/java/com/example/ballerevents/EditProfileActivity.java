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
 * Activity that allows a user to modify and save their profile information.
 *
 * <p>This screen supports editing the following fields:
 * <ul>
 *     <li>Name</li>
 *     <li>Profile photo</li>
 *     <li>About Me</li>
 *     <li>Interests (comma-separated list)</li>
 * </ul>
 *
 * <p>Workflow:
 * <ol>
 *     <li>Loads current user data from Firestore on startup.</li>
 *     <li>Allows the user to select a new profile image using an image picker.</li>
 *     <li>On Save, updates the "users/{uid}" document with modified values.</li>
 * </ol>
 *
 * <p><b>Note:</b> Profile photos selected from the device are stored as a local URI string.
 * For production, this should be uploaded to Firebase Storage and replaced with a download URL.</p>
 */
public class EditProfileActivity extends AppCompatActivity {

    /** ViewBinding for activity_edit_profile.xml */
    private ActivityEditProfileBinding binding;

    /** Firestore instance used to fetch & update user documents */
    private FirebaseFirestore db;

    /** Reference to the Firestore document representing the current user */
    private DocumentReference userRef;

    /** Holds a newly selected profile image URI string (if the user picks one) */
    private String newProfileImageUriString = null;

    /** Image picker launcher for selecting profile photos */
    private ActivityResultLauncher<String> imagePickerLauncher;

    /**
     * Initializes the activity, loads Firestore references, configures image picker,
     * and populates UI with existing profile data.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // Ensure a logged-in user exists
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

    /**
     * Configures the image picker used to select profile pictures.
     * When a user selects an image, the result URI is immediately shown via Glide.
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        newProfileImageUriString = uri.toString();
                        Glide.with(this).load(uri).into(binding.ivProfileImage);
                    }
                }
        );
    }

    /**
     * Loads the current user's profile from Firestore and populates the UI fields.
     * This includes name, about me, interests, and profile picture.
     */
    private void loadCurrentData() {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                if (userProfile != null) {

                    // Name
                    binding.etName.setText(userProfile.getName());

                    // About Me
                    binding.etAboutMe.setText(userProfile.getAboutMe());

                    // Profile Picture
                    if (userProfile.getProfilePictureUrl() != null &&
                            !userProfile.getProfilePictureUrl().isEmpty()) {

                        Glide.with(this)
                                .load(userProfile.getProfilePictureUrl())
                                .placeholder(R.drawable.placeholder_avatar1)
                                .error(R.drawable.placeholder_avatar1)
                                .into(binding.ivProfileImage);
                    }

                    // Interests
                    if (userProfile.getInterests() != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            binding.etInterests.setText(
                                    String.join(", ", userProfile.getInterests())
                            );
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (String interest : userProfile.getInterests()) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(interest);
                            }
                            binding.etInterests.setText(sb.toString());
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Registers UI click listeners:
     * <ul>
     *     <li>Back button closes the screen</li>
     *     <li>Save button triggers {@link #saveProfileData()}</li>
     *     <li>Clicking the profile picture or "Change Photo" opens image picker</li>
     * </ul>
     */
    private void setupListeners() {
        binding.btnBackEdit.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveProfileData());

        binding.ivProfileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.tvChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    /**
     * Reads user-entered profile fields, validates them, converts interests
     * into a list, and updates the Firestore user document.
     *
     * <p>Fields updated:
     * <ul>
     *     <li>name</li>
     *     <li>aboutMe</li>
     *     <li>interests</li>
     *     <li>profilePictureUrl (optional, only if changed)</li>
     * </ul>
     */
    private void saveProfileData() {
        String newName = binding.etName.getText().toString().trim();
        String newAboutMe = binding.etAboutMe.getText().toString().trim();
        String newInterestsString = binding.etInterests.getText().toString();

        // Validation
        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert comma-separated interests back to List<String>
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

        // Build update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("aboutMe", newAboutMe);
        updates.put("interests", newInterests);

        // Only update profile picture if changed
        if (newProfileImageUriString != null) {
            updates.put("profilePictureUrl", newProfileImageUriString);
            // Note: In production, upload to Firebase Storage first, store download URL instead.
        }

        // Commit updates
        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving profile.", Toast.LENGTH_SHORT).show()
                );
    }
}
