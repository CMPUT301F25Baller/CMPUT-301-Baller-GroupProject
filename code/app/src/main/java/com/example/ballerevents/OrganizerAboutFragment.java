package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ballerevents.databinding.FragmentOrganizerAboutBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class OrganizerAboutFragment extends Fragment {

    private static final String TAG = "OrganizerAboutFragment";

    private FragmentOrganizerAboutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ListenerRegistration userListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentOrganizerAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Attach listener in onStart
        if (currentUserId != null) {
            loadOrganizerInfo();
        } else {
            binding.tvAboutMe.setText("Could not load profile. Please log in again.");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Detach listener in onStop
        if (userListener != null) {
            userListener.remove();
        }
    }

    private void loadOrganizerInfo() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                binding.tvAboutMe.setText("Error loading profile.");
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                UserProfile userProfile = snapshot.toObject(UserProfile.class);
                if (userProfile != null && binding != null) {
                    // Set About Me text
                    binding.tvAboutMe.setText(userProfile.getAboutMe());

                    // Populate Interests
                    binding.chipGroupInterests.removeAllViews(); // Clear old chips
                    List<String> interests = userProfile.getInterests();
                    if (interests != null) {
                        for (String interest : interests) {
                            Chip chip = new Chip(getContext());
                            chip.setText(interest);
                            // You can style chips here
                            binding.chipGroupInterests.addView(chip);
                        }
                    }
                }
            } else {
                Log.d(TAG, "No such document");
                binding.tvAboutMe.setText("Profile not found.");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear the binding reference
    }
}