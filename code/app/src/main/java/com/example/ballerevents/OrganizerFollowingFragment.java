package com.example.ballerevents;

import android.content.Intent;
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
 * Fragment displaying Following/Followers lists.
 * Uses ProfilesListAdapter (read-only) to prevent deletion.
 */
public class OrganizerFollowingFragment extends Fragment {

    private static final String TAG = "OrganizerFollowingFrag";

    private FragmentOrganizerFollowingBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    // Changed Adapter Type to the Safe Read-Only Adapter
    private ProfilesListAdapter followingAdapter;
    private ProfilesListAdapter followersAdapter;
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
        binding = FragmentOrganizerFollowingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerViews();

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            binding.emptyFollowing.setVisibility(View.VISIBLE);
            binding.emptyFollowers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (currentUserId != null) {
            loadOrganizerProfile();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (userListener != null) {
            userListener.remove();
        }
    }

    private void setupRecyclerViews() {
        // Safe Adapter: Clicking opens profile details
        followingAdapter = new ProfilesListAdapter(profile -> {
            Intent intent = new Intent(getContext(), ProfileDetailsActivity.class);
            intent.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_ID, profile.getId());
            startActivity(intent);
        });

        binding.rvFollowing.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFollowing.setAdapter(followingAdapter);

        followersAdapter = new ProfilesListAdapter(profile -> {
            Intent intent = new Intent(getContext(), ProfileDetailsActivity.class);
            intent.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_ID, profile.getId());
            startActivity(intent);
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
            }
        });
    }

    private void loadProfileLists(List<String> ids, ProfilesListAdapter adapter, View emptyView, String emptyMessage) {
        if (ids == null || ids.isEmpty()) {
            adapter.submitList(new ArrayList<>());
            emptyView.setVisibility(View.VISIBLE);
            if (emptyView instanceof android.widget.TextView) {
                ((android.widget.TextView) emptyView).setText(emptyMessage);
            }
            return;
        }

        // Firestore 'whereIn' query limited to 10 for safety in this snippet,
        // ideally paginate or chunk if lists are large.
        List<String> chunk = ids.subList(0, Math.min(ids.size(), 10));

        db.collection("users")
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserProfile> profiles = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        UserProfile p = doc.toObject(UserProfile.class);
                        if (p != null) {
                            p.setUid(doc.getId());
                            profiles.add(p);
                        }
                    }
                    adapter.submitList(profiles);
                    emptyView.setVisibility(profiles.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error fetching profile list", e);
                    emptyView.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}