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
 * Fragment that displays the detailed "About" section for an Organizer.
 *
 * <p>Features include:</p>
 * <ul>
 * <li>Displaying the organizer's bio ("About Me").</li>
 * <li>Displaying interests as chips.</li>
 * <li>Navigating to the edit profile screen.</li>
 * <li>Handling user logout.</li>
 * </ul>
 */
public class OrganizerAboutFragment extends Fragment {

    private static final String TAG = "OrganizerAboutFragment";
    private FragmentOrganizerAboutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration userListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOrganizerAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            setupRealtimeProfileListener();
        }

        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class))
        );

        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    /**
     * Sets up a real-time listener to fetch and display profile updates.
     */
    private void setupRealtimeProfileListener() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                UserProfile userProfile = snapshot.toObject(UserProfile.class);

                if (userProfile != null && binding != null) {
                    binding.tvAboutMe.setText(userProfile.getAboutMe());
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
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (userListener != null) {
            userListener.remove();
        }
    }
}