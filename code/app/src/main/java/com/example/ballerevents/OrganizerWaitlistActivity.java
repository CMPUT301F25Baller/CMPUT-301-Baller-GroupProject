package com.example.ballerevents;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerWaitlistBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the current waitlist (entrants who applied to the event lottery)
 * for a given event to an organizer or admin.
 */
public class OrganizerWaitlistActivity extends AppCompatActivity {

    /** Intent extra key for the event whose waitlist we are showing. */
    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";

    private static final String TAG = "OrganizerWaitlistActivity";

    private ActivityOrganizerWaitlistBinding binding;
    private FirebaseFirestore db;

    private String eventId;
    private final List<UserProfile> entrants = new ArrayList<>();
    private WaitlistUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerWaitlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvTitle.setText("Waitlist"); // you can move this to strings.xml later

        adapter = new WaitlistUserAdapter(entrants, this);
        binding.recyclerWaitlist.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerWaitlist.setAdapter(adapter);

        loadWaitlist();
    }

    /**
     * Queries Firestore for all users who have this event in their appliedEventIds array
     * and displays them in the list.
     */
    private void loadWaitlist() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        db.collection("users")
                .whereArrayContains("appliedEventIds", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    entrants.clear();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            UserProfile profile = doc.toObject(UserProfile.class);
                            if (profile != null) {
                                entrants.add(profile);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(entrants.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading waitlist", e);
                    Toast.makeText(this, "Failed to load waitlist", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                });
    }
}
