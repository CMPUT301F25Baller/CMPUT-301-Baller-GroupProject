package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.app.AlertDialog;

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
 * Displays all events created by the logged-in organizer.
 * Features:
 * - View / Edit / Manage Waitlist
 * - NEW: Delete Event functionality
 */
public class OrganizerEventFragment extends Fragment {

    private static final String TAG = "OrganizerEventFragment";

    private FragmentOrganizerEventBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

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
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
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
        if (currentUserId != null) {
            loadOrganizerEvents();
        } else {
            Toast.makeText(getContext(), "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            binding.tvNoEvents.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new TrendingEventAdapter(event -> {
            if (getActivity() instanceof OrganizerActivity) {
                ((OrganizerActivity) getActivity()).setSelectedEventId(event.getId());
            }
            showEventOptionsDialog(event);
        });
        binding.rvOrganizerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrganizerEvents.setAdapter(adapter);
    }

    private void showEventOptionsDialog(Event event) {
        if (getContext() == null) return;

        CharSequence[] options = new CharSequence[]{
                "View Details",
                "Edit Event",
                "Manage Waitlist",
                "Delete Event" // NEW OPTION
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
                        case 2: // Manage Waitlist
                            Intent waitlist = new Intent(getActivity(), OrganizerWaitlistActivity.class);
                            waitlist.putExtra(OrganizerWaitlistActivity.EXTRA_EVENT_ID, event.getId());
                            startActivity(waitlist);
                            break;
                        case 3: // Delete
                            confirmDeleteEvent(event);
                            break;
                    }
                })
                .show();
    }

    // --- DELETE LOGIC ---

    private void confirmDeleteEvent(Event event) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + event.getTitle() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event) {
        if (event.getId() == null) return;

        db.collection("events").document(event.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                        loadOrganizerEvents(); // Refresh list
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to delete event.", Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Error deleting event", e);
                });
    }

    // ---------------------

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
                        if (getActivity() instanceof OrganizerActivity && !events.isEmpty()) {
                            ((OrganizerActivity) getActivity()).setSelectedEventId(events.get(0).getId());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
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