package com.example.ballerevents;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.AdminEventsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminEventsActivity extends AppCompatActivity
        implements AdminEventsAdapter.OnEventActionListener {

    private static final String TAG = "AdminEventsActivity";
    private AdminEventsBinding binding;
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

        binding.tvTitle.setText("Events");
        binding.btnBack.setOnClickListener(v -> finish());

        loadAllEvents();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int start, int b, int c) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllEvents() {
        if (binding.progress != null) binding.progress.setVisibility(View.VISIBLE);

        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    if (binding.progress != null) binding.progress.setVisibility(View.GONE);
                    allEvents = snap.toObjects(Event.class);
                    for (int i = 0; i < snap.size(); i++) {
                        allEvents.get(i).setId(snap.getDocuments().get(i).getId());
                    }
                    adapter.submitList(new ArrayList<>(allEvents));
                })
                .addOnFailureListener(e -> {
                    if (binding.progress != null) binding.progress.setVisibility(View.GONE);
                    Log.w(TAG, "Error", e);
                });
    }

    private void filter(String query) {
        if (query == null) query = "";
        String q = query.toLowerCase();
        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            if (e.getTitle() != null && e.getTitle().toLowerCase().contains(q)) {
                filtered.add(e);
            }
        }
        adapter.submitList(filtered);
    }

    // --- KEY FIX: Use the Correct Extra Key ---
    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
        startActivity(intent);
    }

    @Override
    public void onViewWaitlist(Event event) {
        Intent intent = new Intent(this, OrganizerWaitlistActivity.class);
        intent.putExtra(OrganizerWaitlistActivity.EXTRA_EVENT_ID, event.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event?")
                .setMessage("Delete '" + event.getTitle() + "' permanently?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection("events").document(event.getId()).delete()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                allEvents.remove(event);
                                filter(binding.etSearch.getText().toString());
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}