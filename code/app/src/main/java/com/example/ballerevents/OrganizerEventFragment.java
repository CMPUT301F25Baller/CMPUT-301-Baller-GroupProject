package com.example.ballerevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.FragmentOrganizerEventBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * Fragment showing all events created by the current organizer.
 */
public class OrganizerEventFragment extends Fragment {

    private static final String TAG = "OrganizerEventFragment";

    private FragmentOrganizerEventBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private OrganizerEventsAdapter adapter;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentOrganizerEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUserId != null) {
            loadOrganizerEvents();
        } else {
            Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new OrganizerEventsAdapter(event -> {
            // When organizer taps an event, remember its ID in the activity
            if (getActivity() instanceof OrganizerActivity) {
                ((OrganizerActivity) getActivity()).setSelectedEventId(event.getId());
                Toast.makeText(
                        getContext(),
                        "Selected event: " + event.getTitle(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(adapter);
    }

    private void loadOrganizerEvents() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("events")
                .whereEqualTo("organizerId", currentUserId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    binding.progressBar.setVisibility(View.GONE);
                    List<Event> events = qs.toObjects(Event.class);

                    adapter.submitList(events);

                    // Optional: auto-select first event if exists
                    if (!events.isEmpty() && getActivity() instanceof OrganizerActivity) {
                        ((OrganizerActivity) getActivity()).setSelectedEventId(events.get(0).getId());
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading events.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
