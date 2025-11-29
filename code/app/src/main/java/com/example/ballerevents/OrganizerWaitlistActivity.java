package com.example.ballerevents;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerWaitlistBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
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

        // Setup RecyclerView
        adapter = new WaitlistUserAdapter(entrants, this);
        binding.rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWaitlist.setAdapter(adapter);

        // Setup "Run Lottery" Button (assuming you add a button with ID btn_run_lottery to your XML)
        // If the button doesn't exist in XML yet, this will be null, so check for null or add it to XML.
        if (binding.btnRunLottery != null) {
            binding.btnRunLottery.setOnClickListener(v -> showLotteryDialog());
        }

        if (eventId != null) {
            loadWaitlist();
        } else {
            Toast.makeText(this, "Error: No Event ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Queries Firestore for all users who have this event in their appliedEventIds array
     * and displays them in the list.
     */
    private void loadWaitlist() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        // Find users who have this eventId in their 'appliedEventIds' list
        db.collection("users")
                .whereArrayContains("appliedEventIds", eventId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    entrants.clear();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            UserProfile profile = doc.toObject(UserProfile.class);
                            if (profile != null) {
                                // IMPORTANT: Ensure the ID is set from the document key if not in the object
                                // profile.setId(doc.getId()); // If UserProfile needs manual ID setting
                                entrants.add(profile);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(entrants.isEmpty() ? View.VISIBLE : View.GONE);

                    // Update header count
                    if (binding.tvWaitlistCount != null) {
                        binding.tvWaitlistCount.setText("Waitlist: " + entrants.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading waitlist", e);
                    Toast.makeText(this, "Failed to load waitlist", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                });
    }

    /**
     * Shows a dialog asking how many people to select.
     */
    private void showLotteryDialog() {
        if (entrants.isEmpty()) {
            Toast.makeText(this, "No entrants to sample from.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Run Lottery");
        builder.setMessage("How many entrants do you want to select?");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Select", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                int count = Integer.parseInt(val);
                runLottery(count);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Randomly picks N users and moves them to 'chosenUserIds' in the Event document.
     */
    private void runLottery(int numberToSelect) {
        if (numberToSelect > entrants.size()) {
            Toast.makeText(this, "Cannot select more than waitlist size (" + entrants.size() + ")", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Shuffle list
        List<UserProfile> shuffled = new ArrayList<>(entrants);
        Collections.shuffle(shuffled);

        // 2. Pick top N
        List<UserProfile> selectedUsers = shuffled.subList(0, numberToSelect);
        List<String> selectedIds = new ArrayList<>();
        for (UserProfile u : selectedUsers) {
            // Usually u.id is set by Firestore @DocumentId, but ensure it's accessed correctly
            // If u.getId() returns null, we might need to rely on how we loaded them.
            // Assuming UserProfile has a public getId() or public String id field.
            // Since UserProfile in your code has private id with @DocumentId, but maybe no getter in snippet?
            // Assuming we can get the ID:
            selectedIds.add(u.getId()); // Ensure UserProfile.java has getId()
        }

        if (selectedIds.isEmpty()) return;

        // 3. Update Firestore: Add to 'chosenUserIds' and optionally remove from 'waitlist' logic if needed.
        // For this app, 'waitlist' is calculated by 'appliedEventIds' in USER doc.
        // 'Chosen' is stored in EVENT doc.

        WriteBatch batch = db.batch();

        // Update Event document
        batch.update(db.collection("events").document(eventId),
                "chosenUserIds", FieldValue.arrayUnion(selectedIds.toArray()));

        // OPTIONAL: Send Notification to each user (create a doc in 'notifications' or 'users/{uid}/notifications')
        // This part depends on your exact notification structure.
        // For now, we just do the logic update.

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Lottery complete! " + numberToSelect + " users selected.", Toast.LENGTH_LONG).show();
            // Refresh or navigate
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lottery failed.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lottery commit failed", e);
        });
    }
}
