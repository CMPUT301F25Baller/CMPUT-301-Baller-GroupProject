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

// Import ViewBinding
import com.example.ballerevents.databinding.FragmentOrganizerEventBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * Fragment that displays all events created by the currently authenticated organizer.
 * <p>
 * This fragment loads events from Firestore where <code>organizerId</code> matches the
 * current user ID. Clicking an event opens the editor (OrganizerEventCreationActivity)
 * instead of the read-only details view.
 */
public class OrganizerEventFragment extends Fragment {

    private static final String TAG = "OrganizerEventFragment";

    private FragmentOrganizerEventBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TrendingEventAdapter adapter;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload events when returning from the Create/Edit screen
        if (currentUserId != null) {
            loadOrganizerEvents();
        } else {
            Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            binding.tvNoEvents.setText("Could not load events. Please log in again.");
            binding.tvNoEvents.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        // We reuse TrendingEventAdapter since it's just a card
        adapter = new TrendingEventAdapter(event -> {
            // --- UPDATED CLICK LOGIC ---
            // When an organizer clicks their own event, open the EDITOR (Creation Activity)
            // passing the event ID so it knows to load data.
            Intent intent = new Intent(getActivity(), OrganizerEventCreationActivity.class);
            intent.putExtra(OrganizerEventCreationActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(adapter);
    }

    private void loadOrganizerEvents() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoEvents.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("organizerId", currentUserId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    List<Event> events = queryDocumentSnapshots.toObjects(Event.class);

                    if (events.isEmpty()) {
                        binding.tvNoEvents.setVisibility(View.VISIBLE);
                    } else {
                        adapter.submitList(events);
                    }
                    Log.d(TAG, "Loaded " + events.size() + " events for organizer " + currentUserId);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoEvents.setText("Error loading events.");
                    binding.tvNoEvents.setVisibility(View.VISIBLE);
                    Log.w(TAG, "Error loading organizer events", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}