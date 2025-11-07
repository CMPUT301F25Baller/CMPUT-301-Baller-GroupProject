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

/**
 * Fragment that displays two lists related to the organizer:
 *
 * <ul>
 *     <li><b>Following</b> – Profiles the organizer follows</li>
 *     <li><b>Followers</b> – Profiles that follow the organizer</li>
 * </ul>
 *
 * This fragment listens to changes in the organizer’s Firestore profile
 * and automatically updates follower/following counts and lists.
 * Uses {@link AdminProfilesAdapter} to show profile cards.
 *
 * <p>Lifecycle:
 * <ul>
 *     <li>Listener attached in {@link #onStart()}</li>
 *     <li>Listener removed in {@link #onStop()}</li>
 * </ul>
 */
public class OrganizerFollowingFragment extends Fragment {

    private static final String TAG = "OrganizerFollowingFrag";

    private FragmentOrganizerFollowingBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private AdminProfilesAdapter followingAdapter;
    private AdminProfilesAdapter followersAdapter;

    private ListenerRegistration userListener;

    /**
     * Initializes Firebase instances and retrieves the organizer’s UID.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }
    }

    /**
     * Inflates the layout using ViewBinding.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentOrganizerFollowingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Sets up RecyclerViews and handles cases where the user is not logged in.
     */
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

    /**
     * Attaches the Firestore snapshot listener when the fragment becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Attach listener in onStart
        if (currentUserId != null) {
            loadOrganizerProfile();
        }
    }

    /**
     * Removes the listener to prevent memory leaks when the fragment is stopped.
     */
    @Override
    public void onStop() {
        super.onStop();
        // Detach listener in onStop
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Initializes adapters and RecyclerView layouts for the
     * "Following" and "Followers" lists.
     */
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

    /**
     * Attaches a snapshot listener to the organizer’s profile document.
     * Updates counts and loads the respective lists whenever changes occur.
     */
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
     * Queries Firestore for user profiles whose document IDs match the given list of IDs.
     *
     * @param ids          list of profile IDs to load
     * @param adapter      adapter to display the profiles in a RecyclerView
     * @param emptyView    a placeholder view displayed when the list is empty
     * @param emptyMessage message to show when no profiles exist
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

    /**
     * Clears the ViewBinding reference to prevent memory leaks.
     */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear the binding reference
    }
}