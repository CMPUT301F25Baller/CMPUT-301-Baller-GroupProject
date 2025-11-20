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

import com.example.ballerevents.databinding.FragmentOrganizerFollowingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying two lists for the current organizer:
 *
 * <ul>
 *     <li><b>Following</b> – Users the organizer is following</li>
 *     <li><b>Followers</b> – Users who follow the organizer</li>
 * </ul>
 *
 * <p>This fragment listens directly to the organizer's profile document. Whenever
 * the profile updates (e.g., follower count changes), the UI automatically refreshes.
 * </p>
 *
 * <p>The lists reuse {@link AdminProfilesAdapter} to display user profile cards.</p>
 *
 * <p>Lifecycle behavior:</p>
 * <ul>
 *     <li>Firestore listener attached in {@link #onStart()}</li>
 *     <li>Listener removed in {@link #onStop()}</li>
 * </ul>
 */
public class OrganizerFollowingFragment extends Fragment {

    /** Logging tag for debugging. */
    private static final String TAG = "OrganizerFollowingFrag";

    /** ViewBinding for accessing layout views. */
    private FragmentOrganizerFollowingBinding binding;

    /** Firestore instance for loading user profiles. */
    private FirebaseFirestore db;

    /** FirebaseAuth instance for the current organizer. */
    private FirebaseAuth mAuth;

    /** UID of the logged-in organizer, or null if not authenticated. */
    private String currentUserId;

    /** Adapter for the "Following" list. */
    private AdminProfilesAdapter followingAdapter;

    /** Adapter for the "Followers" list. */
    private AdminProfilesAdapter followersAdapter;

    /** Firestore listener registration to detach when fragment stops. */
    private ListenerRegistration userListener;

    /**
     * Initializes Firebase components and retrieves the organizer’s UID.
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
     * Inflates the UI layout with ViewBinding.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentOrganizerFollowingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Once the view is created, the adapters are initialized and empty views
     * are handled if no user is logged in.
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
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
     * Attaches Firestore snapshot listener when fragment becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (currentUserId != null) {
            loadOrganizerProfile();
        }
    }

    /**
     * Removes the Firestore listener to prevent memory leaks.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (userListener != null) {
            userListener.remove();
        }
    }

    /**
     * Sets up RecyclerViews for the following and followers lists.
     */
    private void setupRecyclerViews() {

        // Adapter: Following
        followingAdapter = new AdminProfilesAdapter(profile -> {
            // TODO: Navigate to this user's profile
        });
        binding.rvFollowing.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFollowing.setAdapter(followingAdapter);

        // Adapter: Followers
        followersAdapter = new AdminProfilesAdapter(profile -> {
            // TODO: Navigate to this user's profile
        });
        binding.rvFollowers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFollowers.setAdapter(followersAdapter);
    }

    /**
     * Adds a real-time listener on the organizer's user profile.
     * Whenever the user’s following or follower lists change, the UI updates.
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

                    // Load lists from Firestore
                    loadProfileLists(
                            userProfile.getFollowingIds(),
                            followingAdapter,
                            binding.emptyFollowing,
                            "Not following anyone yet."
                    );

                    loadProfileLists(
                            userProfile.getFollowerIds(),
                            followersAdapter,
                            binding.emptyFollowers,
                            "No followers yet."
                    );
                }
            } else {
                Log.d(TAG, "No such document");
            }
        });
    }

    /**
     * Queries Firestore for profiles whose document IDs appear in the provided list.
     *
     * @param ids          The list of document IDs to fetch.
     * @param adapter      The adapter that will display the fetched profiles.
     * @param emptyView    A placeholder TextView displayed when data is empty.
     * @param emptyMessage The message shown when the list contains no entries.
     */
    private void loadProfileLists(
            List<String> ids,
            AdminProfilesAdapter adapter,
            View emptyView,
            String emptyMessage
    ) {
        if (ids == null || ids.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            emptyView.setVisibility(View.VISIBLE);

            if (emptyView instanceof android.widget.TextView) {
                ((android.widget.TextView) emptyView).setText(emptyMessage);
            }

            return;
        }

        // Query Firestore for matching profiles
        db.collection("users")
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> profiles = queryDocumentSnapshots.toObjects(UserProfile.class);
                    adapter.submitList(profiles);

                    emptyView.setVisibility(profiles.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error fetching profile list", e);
                    emptyView.setVisibility(View.VISIBLE);

                    if (emptyView instanceof android.widget.TextView) {
                        ((android.widget.TextView) emptyView).setText("Error loading list.");
                    }
                });
    }

    /**
     * Clears ViewBinding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
