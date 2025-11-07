package com.example.ballerevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a simple list of followed user IDs (prototype). Replace with a richer adapter
 * if you want to render names/avatars by fetching each user doc.
 */
public class OrganizerFollowingFragment extends Fragment {

    private static final String ARG_ORG_ID = "org_id";
    private String organizerId;

    private RecyclerView rv;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SimpleTextAdapter adapter;

    public static OrganizerFollowingFragment newInstance(String organizerId) {
        OrganizerFollowingFragment f = new OrganizerFollowingFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ORG_ID, organizerId);
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        organizerId = getArguments() != null ? getArguments().getString(ARG_ORG_ID) : null;
    }

    @Nullable
    @Override public View onCreateView(@NonNull LayoutInflater inflater,
                                       @Nullable ViewGroup container,
                                       @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organizer_following, container, false);
        rv = v.findViewById(R.id.rvFollowing);
        progressBar = v.findViewById(R.id.progressBar);
        emptyView = v.findViewById(R.id.emptyView);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SimpleTextAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        fetchFollowing();
        return v;
    }

    private void fetchFollowing() {
        if (organizerId == null) { showEmpty("No organizer."); return; }
        showLoading(true);

        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("users").document(organizerId);

        userRef.get()
                .addOnSuccessListener(doc -> {
                    showLoading(false);
                    if (!doc.exists()) { showEmpty("Organizer not found."); return; }
                    UserProfile up = doc.toObject(UserProfile.class);
                    List<String> following = (up != null && up.getFollowing() != null)
                            ? up.getFollowing() : new ArrayList<>();

                    adapter = new SimpleTextAdapter(following);
                    rv.setAdapter(adapter);

                    if (following.isEmpty()) showEmpty("Not following anyone yet.");
                    else emptyView.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmpty("Failed to load following list.");
                });
    }

    private void showLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(String msg) {
        if (emptyView != null) {
            emptyView.setText(msg);
            emptyView.setVisibility(View.VISIBLE);
        }
    }
}
