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
 * current user ID, and shows them inside a RecyclerView using {@link TrendingEventAdapter}.
 * <p>
 * If no events are found, a helper message is shown. If the user is not logged in,
 * an error message is displayed instead.
 */
public class OrganizerEventFragment extends Fragment {

    private static final String TAG = "OrganizerEventFragment";

    private FragmentOrganizerEventBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TrendingEventAdapter adapter;
    private String currentUserId;

    /**
     * Initializes Firebase instances and retrieves the authenticated
     * organizer's UID (if logged in).
     *
     * @param savedInstanceState previously saved fragment state, if any
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
     * Inflates the fragment layout using ViewBinding.
     *
     * @param inflater  LayoutInflater to inflate the XML
     * @param container parent view group
     * @param savedInstanceState saved UI state, if any
     * @return root fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout using ViewBinding
        binding = FragmentOrganizerEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * After the view is created, sets up the RecyclerView and loads the organizer's events.
     *
     * @param view the root view
     * @param savedInstanceState saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup the RecyclerView
        setupRecyclerView();

        // Load this organizer's events from Firestore
        if (currentUserId != null) {
            loadOrganizerEvents();
        } else {
            Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            binding.tvNoEvents.setText("Could not load events. Please log in again.");
            binding.tvNoEvents.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configures the RecyclerView and attaches the {@link TrendingEventAdapter}.
     * Clicking an item opens {@link DetailsActivity} for that event.
     */
    private void setupRecyclerView() {
        // We can reuse TrendingEventAdapter since it's just a card
        adapter = new TrendingEventAdapter(event -> {
            // When an organizer clicks their own event, open DetailsActivity
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        });

        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(adapter);
    }

    /**
     * Queries Firestore for all events created by the current organizer and updates the UI.
     * <p>
     * Results are sorted by the event's <code>date</code> field in descending order.
     * Displays the appropriate empty state or error message if needed.
     */
    private void loadOrganizerEvents() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoEvents.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("organizerId", currentUserId) // Query for this organizer's events
                .orderBy("date", Query.Direction.DESCENDING) // Show newest first
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

    /**
     * Clears the ViewBinding reference to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear the binding reference
    }
}