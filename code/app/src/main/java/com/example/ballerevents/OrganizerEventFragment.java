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
 * an event, the fragment navigates to {@link OrganizerEventCreationActivity} in
 * edit mode, passing the Firestore document ID.
 * </p>
 *
 * <p>The fragment automatically refreshes events in {@link #onResume()} so any
 * changes made in the editor screen are immediately reflected when returning.</p>
 */

public class OrganizerEventFragment extends Fragment {

    private static final String TAG = "OrganizerEventFragment";

    private FragmentOrganizerEventBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // USE TRENDING EVENT ADAPTER (Team’s UI)
    private TrendingEventAdapter adapter;

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
            binding.tvNoEvents.setText("Could not load events. Please log in again.");
            binding.tvNoEvents.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up RecyclerView with the teammate’s modern card adapter.
     * Clicking an event opens the options dialog AND also sends the eventId
     * up to OrganizerActivity (so lottery screens know the selected event).
     */
    private void setupRecyclerView() {

        adapter = new TrendingEventAdapter(event -> {

            // Also send event ID to Activity (your branch logic)
            if (getActivity() instanceof OrganizerActivity) {
                ((OrganizerActivity) getActivity()).setSelectedEventId(event.getId());
            }

            // Then open the options dialog
            showEventOptionsDialog(event);
        });

        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(adapter);
    }

    /**
     * Merged dialog:
     *  ▪ Edit Event
     *  ▪ View Waitlist
     * (Matches teammate’s design, preserves your logic)
     */
    private void showEventOptionsDialog(Event event) {

        if (getContext() == null) return;

        CharSequence[] options = new CharSequence[]{
                "Edit Event",
                "View Waitlist"
        };

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(event.getTitle())
                .setItems(options, (dialog, which) -> {

                    switch (which) {

                        case 0: // EDIT EVENT
                            Intent editIntent = new Intent(getActivity(), OrganizerEventCreationActivity.class);
                            editIntent.putExtra(OrganizerEventCreationActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(editIntent);
                            break;

                        case 1: // VIEW WAITLIST
                            Intent waitIntent = new Intent(getActivity(), OrganizerWaitlistActivity.class);
                            waitIntent.putExtra(OrganizerWaitlistActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(waitIntent);
                            break;
                    }

                })
                .show();
    }

    /**
     * Loads all events owned by the organizer.
     */
    private void loadOrganizerEvents() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoEvents.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("organizerId", currentUserId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    binding.progressBar.setVisibility(View.GONE);

                    List<Event> events = snapshot.toObjects(Event.class);

                    if (events.isEmpty()) {
                        binding.tvNoEvents.setVisibility(View.VISIBLE);
                    } else {
                        adapter.submitList(events);

                        // Auto-select first (your branch behavior)
                        if (getActivity() instanceof OrganizerActivity) {
                            ((OrganizerActivity) getActivity())
                                    .setSelectedEventId(events.get(0).getId());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoEvents.setText("Error loading events.");
                    binding.tvNoEvents.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error loading organizer events", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
