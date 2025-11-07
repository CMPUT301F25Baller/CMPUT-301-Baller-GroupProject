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
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = db.collection("users").document(currentUserId);

        loadCurrentData();
        setupListeners();
    }

    private void loadCurrentData() {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    binding.etAboutMe.setText(userProfile.getAboutMe());

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
    }

    private void saveProfileData() {
        String newAboutMe = binding.etAboutMe.getText().toString();
        String newInterestsString = binding.etInterests.getText().toString();

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
                if (!trimmed.isEmpty()) {
                    newInterests.add(trimmed);
                }
            }
        }

        // Update the repository
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