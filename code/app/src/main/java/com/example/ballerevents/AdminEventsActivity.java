package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.AdminEventsBinding; // Assuming layout is admin_events.xml
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminEventsActivity extends AppCompatActivity implements AdminEventsAdapter.OnEventActionListener {

    private static final String TAG = "AdminEventsActivity";
    private AdminEventsBinding binding; // Use ViewBinding
    private FirebaseFirestore db;
    private AdminEventsAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        adapter = new AdminEventsAdapter(this);

        binding.rvEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvEvents.setAdapter(adapter);

        binding.topAppBar.setTitle("Events");
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        loadAllEvents();

        // Search filter (title)
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllEvents() {
        binding.progress.setVisibility(View.VISIBLE);
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progress.setVisibility(View.GONE);
                    allEvents = queryDocumentSnapshots.toObjects(Event.class);
                    adapter.submitList(new ArrayList<>(allEvents)); // Submit a new copy
                })
                .addOnFailureListener(e -> {
                    binding.progress.setVisibility(View.GONE);
                    Log.w(TAG, "Error loading all events", e);
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                });
    }

    private void filter(String query) {
        if (query == null) query = "";
        String q = query.toLowerCase();

        List<Event> filtered;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            filtered = allEvents.stream()
                    .filter(e -> e.getTitle() != null && e.getTitle().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>();
            for (Event e : allEvents) {
                if (e.getTitle() != null && e.getTitle().toLowerCase().contains(q)) {
                    filtered.add(e);
                }
            }
        }
        adapter.submitList(filtered);
    }

    @Override
    public void onDelete(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event?")
                .setMessage("Are you sure you want to permanently delete '" + event.getTitle() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteEventFromFirestore(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEventFromFirestore(Event event) {
        db.collection("events").document(event.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                    allEvents.remove(event); // Remove from local cache
                    filter(binding.etSearch.getText().toString()); // Refresh the filtered list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error deleting event", e);
                });
    }
}