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

import com.example.ballerevents.databinding.FragmentOrganizerEventBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * Fragment displaying all events created by the currently authenticated organizer.
 * <p>
 * This screen is accessible through the organizer dashboard and shows event cards
 * in a vertical list using {@link TrendingEventAdapter}. When an organizer taps
 * an event, an options dialog is shown allowing them to either edit the event
 * or view its waitlist.
 * </p>
 *
 * <p>The fragment automatically refreshes events in {@link #onResume()} so any
 * changes made in the editor screen are immediately reflected when returning.</p>
 */
public class OrganizerEventFragment extends Fragment {

    /** Logging tag for debugging event loading. */
    private static final String TAG = "OrganizerEventFragment";

    /** ViewBinding for accessing layout views. */
    private FragmentOrganizerEventBinding binding;

    /** Firestore instance for reading event documents. */
    private FirebaseFirestore db;

    /** FirebaseAuth instance for identifying the current organizer. */
    private FirebaseAuth mAuth;

    /** Adapter used to render the organizer's event cards. */
    private TrendingEventAdapter adapter;

    /** ID of the currently authenticated organizer. */
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

        // Reload list after returning from creation/editing screens
        if (currentUserId != null) {
            loadOrganizerEvents();
        } else {
            Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            binding.tvNoEvents.setText("Could not load events. Please log in again.");
            binding.tvNoEvents.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets up the RecyclerView and attaches a {@link TrendingEventAdapter} where
     * clicking an item shows options to edit the event or view its waitlist.
     */
    private void setupRecyclerView() {
        adapter = new TrendingEventAdapter(this::showEventOptionsDialog);

        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(adapter);
    }

    /**
     * Shows an options dialog for the selected event, allowing the organizer
     * to either edit the event or view its waitlist.
     *
     * @param event the event that was tapped
     */
    private void showEventOptionsDialog(Event event) {
        if (getContext() == null) {
            return;
        }

        CharSequence[] options = new CharSequence[] {
                "Edit Event",
                "View Waitlist"
        };

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(event.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit Event
                            Intent editIntent = new Intent(getActivity(), OrganizerEventCreationActivity.class);
                            editIntent.putExtra(OrganizerEventCreationActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(editIntent);
                            break;
                        case 1: // View Waitlist
                            Intent waitlistIntent = new Intent(getActivity(), OrganizerWaitlistActivity.class);
                            waitlistIntent.putExtra(OrganizerWaitlistActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(waitlistIntent);
                            break;
                        default:
                            break;
                    }
                })
                .show();
    }

    /**
     * Loads all events from Firestore where {@code organizerId} matches the current organizer.
     * Updates the RecyclerView and handles empty or failure states.
     */
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
        binding = null; // Avoid memory leaks
    }
}
