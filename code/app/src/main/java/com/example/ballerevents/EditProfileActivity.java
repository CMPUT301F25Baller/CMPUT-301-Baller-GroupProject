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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            finish(); // or startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        String currentUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId);

        loadCurrentData();
        setupListeners();
    }

    private void loadCurrentData() {
        userRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        UserProfile p = doc.toObject(UserProfile.class);
                        if (p != null) {
                            binding.etAboutMe.setText(p.getAboutMe() == null ? "" : p.getAboutMe());
                            if (p.getInterests() != null && !p.getInterests().isEmpty()) {
                                String interests = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N
                                        ? String.join(", ", p.getInterests())
                                        : joinComma(p.getInterests());
                                binding.etInterests.setText(interests);
                            }
                        }
                    } else {
                        // Create a stub so updates won’t fail
                        Map<String, Object> stub = new HashMap<>();
                        stub.put("aboutMe", "");
                        stub.put("interests", new java.util.ArrayList<String>());
                        userRef.set(stub, com.google.firebase.firestore.SetOptions.merge());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile.", Toast.LENGTH_SHORT).show()
                );
    }

    private String joinComma(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String it : items) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(it);
        }
        return sb.toString();
    }


    private void setupListeners() {
        binding.btnBackEdit.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void saveProfileData() {
        String newAboutMe = String.valueOf(binding.etAboutMe.getText()).trim();
        String newInterestsString = String.valueOf(binding.etInterests.getText());

        List<String> newInterests;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newInterests = java.util.Arrays.stream(newInterestsString.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
        } else {
            newInterests = new java.util.ArrayList<>();
            for (String s : newInterestsString.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) newInterests.add(t);
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("aboutMe", newAboutMe);
        updates.put("interests", newInterests);

        userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error saving profile.", Toast.LENGTH_SHORT).show()
                );
    }
}