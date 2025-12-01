package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.app.AlertDialog;    // for simple dialogs

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
 * Displays all events created by the logged-in organizer. Uses the team’s
 * TrendingEventAdapter to show event cards, and tapping a card opens an options
 * dialog. Includes editing, viewing waitlist, and sending winner notifications.
 * Automatically refreshes in onResume and updates OrganizerActivity with the
 * selected event ID.
 */
public class OrganizerEventFragment extends Fragment {

    private static final String TAG = "OrganizerEventFragment";

    private FragmentOrganizerEventBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TrendingEventAdapter adapter;
    private String currentUserId;

    /** Repository for sending notifications to winners. */
    private FirestoreEventRepository eventRepository;

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
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }

        eventRepository = new FirestoreEventRepository();
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

    // -------------------------------------------------------------
    // RECYCLER SETUP
    // -------------------------------------------------------------
    private void setupRecyclerView() {

        adapter = new TrendingEventAdapter(event -> {

            // When selecting an event → update OrganizerActivity's selected event
            if (getActivity() instanceof OrganizerActivity) {
                ((OrganizerActivity) getActivity()).setSelectedEventId(event.getId());
            }

            showEventOptionsDialog(event);
        });

        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(adapter);
    }

    // -------------------------------------------------------------
    // EVENT OPTIONS DIALOG (FULL MERGE)
    // -------------------------------------------------------------
    private void showEventOptionsDialog(Event event) {

        if (getContext() == null) return;

        // FINAL MERGED OPTIONS (ALL FEATURES):
        CharSequence[] options = new CharSequence[]{
                "View Details",
                "Edit Event",
                "View Waitlist",
                "Send Winner Notifications"
        };

        new AlertDialog.Builder(getContext())
                .setTitle(event.getTitle())
                .setItems(options, (dialog, which) -> {

                    switch (which) {

                        case 0: // View Details
                            Intent details = new Intent(getActivity(), DetailsActivity.class);
                            details.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(details);
                            break;

                        case 1: // Edit
                            Intent edit = new Intent(getActivity(), OrganizerEventCreationActivity.class);
                            edit.putExtra(OrganizerEventCreationActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(edit);
                            break;

                        case 2: // View Waitlist
                            Intent waitlist = new Intent(getActivity(), OrganizerWaitlistActivity.class);
                            waitlist.putExtra(OrganizerWaitlistActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(waitlist);
                            break;

                        case 3: // Send Winner Notifications
                            sendWinnerNotifications(event);
                            break;
                    }

                })
                .show();
    }

    // -------------------------------------------------------------
    // SEND WINNER NOTIFICATIONS (YOUR BRANCH FEATURE)
    // -------------------------------------------------------------
    private void sendWinnerNotifications(Event event) {
        if (eventRepository == null) return;

        eventRepository.sendWinnerNotifications(
                event.getId(),
                new FirestoreEventRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        if (getContext() != null) {
                            Toast.makeText(getContext(),
                                    "Winner notifications sent.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(),
                                    "Failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        Log.e(TAG, "sendWinnerNotifications error", e);
                    }
                }
        );
    }

    // -------------------------------------------------------------
    // LOAD ORGANIZER EVENTS
    // -------------------------------------------------------------
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

                        // Your branch: auto-select first event
                        if (getActivity() instanceof OrganizerActivity) {
                            ((OrganizerActivity) getActivity())
                                    .setSelectedEventId(events.get(0).getId());
                        }
                    }

                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvNoEvents.setVisibility(View.VISIBLE);
                    binding.tvNoEvents.setText("Error loading events.");
                    Log.e(TAG, "Error loading organizer events", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }
}
