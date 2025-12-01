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
 * <ul>
 * <li><b>Following</b> – Users the organizer is following.</li>
 * <li><b>Followers</b> – Users who follow the organizer.</li>
 * </ul>
 *
 * <p><b>Update:</b> The numerical counts for these lists are now displayed in the
 * parent {@link OrganizerActivity} header to avoid duplication. This fragment
 * focuses solely on rendering the lists using {@link AdminProfilesAdapter}.</p>
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
     * Called to do initial creation of a fragment.
     * Initializes Firebase instances and retrieves current user ID.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
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
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state.
     * @return Return the View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizerFollowingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView}.
     * Sets up the RecyclerViews and handles the case where the user is not logged in.
     *
     * @param view The View returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state.
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
     * Called when the Fragment is visible to the user.
     * Starts listening for real-time updates to the organizer's profile.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (currentUserId != null) {
            loadOrganizerProfile();
        }
    }

    /**
     * Called when the Fragment is no longer started.
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
     * Configures the RecyclerViews for "Following" and "Followers" lists.
     * Sets the LayoutManager and Adapter for each.
     */
    private void setupRecyclerViews() {
        // Adapter: Following
        followingAdapter = new AdminProfilesAdapter(profile -> {
            // TODO: Implement navigation to the clicked user's profile
        });
        binding.rvFollowing.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFollowing.setAdapter(followingAdapter);

        // Adapter: Followers
        followersAdapter = new AdminProfilesAdapter(profile -> {
            // TODO: Implement navigation to the clicked user's profile
        });
        binding.rvFollowers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFollowers.setAdapter(followersAdapter);
    }

    /**
     * Attaches a real-time listener to the current user's document.
     * <p>
     * When the document updates (e.g., a new follower is added), this method:
     * <ol>
     * <li>Extracts the lists of following/follower IDs.</li>
     * <li>Calls {@link #loadProfileLists} to fetch the full profile data.</li>
     * </ol>
     * Note: Stat counting logic has been removed from here as it resides in {@link OrganizerActivity}.
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
                    // Load lists from Firestore based on the IDs found in the profile
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
     * Helper method to query Firestore for a list of UserProfiles.
     *
     * @param ids          The list of document IDs (UIDs) to fetch.
     * @param adapter      The adapter to populate with results.
     * @param emptyView    The TextView to show if the result is empty.
     * @param emptyMessage The message to display in the emptyView.
     */
    private void loadProfileLists(List<String> ids, AdminProfilesAdapter adapter, View emptyView, String emptyMessage) {
        if (ids == null || ids.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            emptyView.setVisibility(View.VISIBLE);
            if (emptyView instanceof android.widget.TextView) {
                ((android.widget.TextView) emptyView).setText(emptyMessage);
            }
            return;
        }

        // Firestore 'whereIn' query to fetch full profile objects for the list of IDs
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
     * Cleans up the binding when the view is destroyed to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}