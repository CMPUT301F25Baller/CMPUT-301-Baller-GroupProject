package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

// Import ViewBinding and correct classes
import com.example.ballerevents.databinding.FragmentOrganizerFollowingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FieldPath; // Import FieldPath

import java.util.ArrayList;
import java.util.List;

public class OrganizerFollowingFragment extends Fragment {

    private static final String TAG = "OrganizerFollowingFrag";

    private FragmentOrganizerFollowingBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private AdminProfilesAdapter followingAdapter;
    private AdminProfilesAdapter followersAdapter;

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
        binding = FragmentOrganizerFollowingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            binding.emptyFollowing.setText("Could not load. Please log in again.");
            binding.emptyFollowing.setVisibility(View.VISIBLE);
            binding.emptyFollowers.setText("Could not load. Please log in again.");
            binding.emptyFollowers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Attach listener in onStart
        if (currentUserId != null) {
            loadOrganizerProfile();
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

    private void setupRecyclerViews() {
        // Adapter for "Following" list
        followingAdapter = new AdminProfilesAdapter(profile -> {
            // TODO: Go to this user's profile
        });
        binding.rvFollowing.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFollowing.setAdapter(followingAdapter);

        // Adapter for "Followers" list
        followersAdapter = new AdminProfilesAdapter(profile -> {
            // TODO: Go to this user's profile
        });
        binding.rvFollowers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFollowers.setAdapter(followersAdapter);
    }

    private void loadOrganizerProfile() {
        DocumentReference userRef = db.collection("users").document(currentUserId);

        userListener = userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                UserProfile userProfile = snapshot.toObject(UserProfile.class);
                if (userProfile != null) {
                    // Update counts
                    binding.tvFollowingCount.setText(String.valueOf(userProfile.getFollowingCount()));
                    binding.tvFollowerCount.setText(String.valueOf(userProfile.getFollowerCount()));

                    // Load the lists of profiles
                    loadProfileLists(userProfile.getFollowingIds(), followingAdapter, binding.emptyFollowing, "Not following anyone yet.");
                    loadProfileLists(userProfile.getFollowerIds(), followersAdapter, binding.emptyFollowers, "No followers yet.");
                }
            } else {
                Log.d(TAG, "No such document");
            }
        });
    }

    /**
     * Fetches a list of UserProfile objects based on a list of IDs.
     */
    private void loadProfileLists(List<String> ids, AdminProfilesAdapter adapter, View emptyView, String emptyMessage) {
        if (ids == null || ids.isEmpty()) {
            adapter.submitList(new ArrayList<>()); // Clear the list
            emptyView.setVisibility(View.VISIBLE);
            if(emptyView instanceof android.widget.TextView) {
                ((android.widget.TextView) emptyView).setText(emptyMessage);
            }
            return;
        }

        // Fetch user profiles where the document ID is in our list
        db.collection("users").whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> profiles = queryDocumentSnapshots.toObjects(UserProfile.class);
                    adapter.submitList(profiles);
                    emptyView.setVisibility(profiles.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error fetching profile list", e);
                    emptyView.setVisibility(View.VISIBLE);
                    if(emptyView instanceof android.widget.TextView) {
                        ((android.widget.TextView) emptyView).setText("Error loading list.");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear the binding reference
    }
}