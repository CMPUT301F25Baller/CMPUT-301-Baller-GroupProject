package com.example.ballerevents;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerFinalEntrantsBinding;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the list of entrants who have been chosen in the lottery.
 * This corresponds to users whose IDs are in the event's {@code chosenUserIds} list.
 */
public class OrganizerFinalEntrantsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final String TAG = "OrganizerFinalEntrants";

    private ActivityOrganizerFinalEntrantsBinding binding;
    private FirebaseFirestore db;

    private String eventId;
    private OrganizerFinalEntrantsAdapter adapter;
    private final List<UserProfile> finalEntrantProfiles = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerFinalEntrantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecycler();
        setupToolbar();
        fetchFinalEntrants();
    }

    private void setupToolbar() {
        binding.toolbarFinalEntrants.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        adapter = new OrganizerFinalEntrantsAdapter(finalEntrantProfiles);
        binding.recyclerFinalEntrants.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerFinalEntrants.setAdapter(adapter);
    }

    /**
     * Loads the event document to retrieve the {@code chosenUserIds} list.
     * Subsequently fetches the {@link UserProfile} documents for those IDs.
     */
    private void fetchFinalEntrants() {
        binding.tvMessage.setText("");
        binding.tvMessage.setVisibility(android.view.View.GONE);

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Event event = snapshot.toObject(Event.class);
                    if (event == null) {
                        binding.tvMessage.setText("Event not found.");
                        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
                        return;
                    }

                    List<String> chosenIds = event.getChosenUserIds();
                    if (chosenIds == null || chosenIds.isEmpty()) {
                        binding.tvMessage.setText("No chosen entrants yet.");
                        binding.tvMessage.setVisibility(android.view.View.VISIBLE);
                        finalEntrantProfiles.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    db.collection("users")
                            .whereIn(FieldPath.documentId(), chosenIds)
                            .get()
                            .addOnSuccessListener(userSnap -> {
                                finalEntrantProfiles.clear();
                                for (var d : userSnap.getDocuments()) {
                                    UserProfile u = d.toObject(UserProfile.class);
                                    if (u != null) {
                                        finalEntrantProfiles.add(u);
                                    }
                                }

                                if (finalEntrantProfiles.isEmpty()) {
                                    binding.tvMessage.setText("No chosen entrants found.");
                                    binding.tvMessage.setVisibility(android.view.View.VISIBLE);
                                } else {
                                    binding.tvMessage.setVisibility(android.view.View.GONE);
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading chosen entrants users", e);
                                binding.tvMessage.setText("Error loading entrants.");
                                binding.tvMessage.setVisibility(android.view.View.VISIBLE);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading event", e);
                    binding.tvMessage.setText("Error loading entrants.");
                    binding.tvMessage.setVisibility(android.view.View.VISIBLE);
                });
    }
}