package com.example.ballerevents;

import android.content.Intent;
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

/**
 * Fragment that displays the authenticated organizer’s profile information,
 * including the "About Me" section and a dynamic list of interest chips.
 * <p>
 * This fragment attaches a real-time Firestore listener to the user's
 * document, keeping the profile information updated automatically. A button
 * is provided to navigate to {@link EditProfileActivity} for editing profile
 * details.
 * </p>
 */
public class OrganizerAboutFragment extends Fragment {

    /** Logging tag for debugging Firestore updates. */
    private static final String TAG = "OrganizerAboutFragment";

    /** ViewBinding for interacting with fragment UI elements. */
    private FragmentOrganizerAboutBinding binding;

    /** Firestore instance for reading profile data. */
    private FirebaseFirestore db;

    /** FirebaseAuth used to retrieve the currently authenticated user. */
    private FirebaseAuth mAuth;

    /** UID of the currently authenticated user (organizer). */
    private String currentUserId;

    /** Active listener registration for the user document. */
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
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout using ViewBinding
        binding = FragmentOrganizerAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // Edit Profile button opens EditProfileActivity
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Attach Firestore listener when fragment becomes visible
        if (currentUserId != null) {
            loadOrganizerInfo();
        } else {
            binding.tvAboutMe.setText("Could not load profile. Please log in again.");
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Detach Firestore listener to avoid memory leaks
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Attaches a real-time listener to the organizer's Firestore document and
     * updates the UI whenever profile data changes.
     */
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
                    // Update About Me
                    binding.tvAboutMe.setText(userProfile.getAboutMe());

                    // Update Interests chip group
                    binding.chipGroupInterests.removeAllViews();
                    List<String> interests = userProfile.getInterests();

                    if (interests != null) {
                        for (String interest : interests) {
                            Chip chip = new Chip(getContext());
                            chip.setText(interest);
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
        // Avoid memory leaks by clearing binding reference
        binding = null;
    }
}
