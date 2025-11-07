package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ballerevents.databinding.ActivityEditProfileBinding;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private UserProfile currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadCurrentData();
        setupListeners();
    }

    private void loadCurrentData() {
        currentUserProfile = EventRepository.getUserProfile(EventRepository.MOCK_USER_ID);
        if (currentUserProfile == null) {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.etAboutMe.setText(currentUserProfile.getAboutMe());

        // Convert List<String> to a single comma-separated string
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            String interests = String.join(", ", currentUserProfile.getInterests());
            binding.etInterests.setText(interests);
        } else {
            // Fallback for older Android versions
            StringBuilder interestsBuilder = new StringBuilder();
            for (String interest : currentUserProfile.getInterests()) {
                if (interestsBuilder.length() > 0) {
                    interestsBuilder.append(", ");
                }
                interestsBuilder.append(interest);
            }
            binding.etInterests.setText(interestsBuilder.toString());
        }
    }

    private void setupListeners() {
        binding.btnBackEdit.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void saveProfileData() {
        String newAboutMe = binding.etAboutMe.getText().toString();
        String newInterestsString = binding.etInterests.getText().toString();

        // Convert comma-separated string back to List<String>
        List<String> newInterests = Arrays.stream(newInterestsString.split(","))
                .map(String::trim) // Remove whitespace
                .filter(s -> !s.isEmpty()) // Remove empty strings
                .collect(Collectors.toList());

        // Update the repository
        EventRepository.updateUserProfile(EventRepository.MOCK_USER_ID, newAboutMe, newInterests);

        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
        finish(); // Go back to ProfileActivity
    }
}