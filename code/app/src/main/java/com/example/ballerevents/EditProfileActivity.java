package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
 * Allows the user to edit their "About Me" and "Interests" fields.
 * Fetches the current profile data on load and saves it back to
 * Firestore on button click.
 */
public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private DocumentReference userRef;

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

        loadCurrentData();
        setupListeners();
    }

    /**
     * Fetches the current user's profile data from Firestore to populate
     * the "About Me" and "Interests" text fields.
     */
    private void loadCurrentData() {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    binding.etAboutMe.setText(userProfile.getAboutMe());

                    if (userProfile.getInterests() != null) {
                        // Convert List<String> to a single comma-separated string
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            String interests = String.join(", ", userProfile.getInterests());
                            binding.etInterests.setText(interests);
                        } else {
                            // Fallback for older Android versions
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

    /**
     * Sets click listeners for the "Back" and "Save" buttons.
     */
    private void setupListeners() {
        binding.btnBackEdit.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveProfileData());
    }

    /**
     * Reads the new data from the text fields, converts the interests
     * string back into a List, and saves the updated data to Firestore.
     */
    private void saveProfileData() {
        String newAboutMe = binding.etAboutMe.getText().toString();
        String newInterestsString = binding.etInterests.getText().toString();

        List<String> newInterests;

        // Convert comma-separated string back to List<String>
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newInterests = Arrays.stream(newInterestsString.split(","))
                    .map(String::trim) // Remove whitespace
                    .filter(s -> !s.isEmpty()) // Remove empty strings
                    .collect(Collectors.toList());
        } else {
            // Fallback for older Android versions
            newInterests = new java.util.ArrayList<>();
            for (String s : newInterestsString.split(",")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    newInterests.add(trimmed);
                }
            }
        }

        // Use a Map to update only specific fields
        Map<String, Object> updates = new HashMap<>();
        updates.put("aboutMe", newAboutMe);
        updates.put("interests", newInterests);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to ProfileActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile.", Toast.LENGTH_SHORT).show();
                });
    }
}