package com.example.ballerevents;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityOrganizerLotteryWinnersBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrganizerLotteryWinnersActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "eventId";

    private ActivityOrganizerLotteryWinnersBinding binding;
    private FirebaseFirestore db;

    private final List<UserProfile> winners = new ArrayList<>();
    private OrganizerLotteryWinnersAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrganizerLotteryWinnersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        adapter = new OrganizerLotteryWinnersAdapter(winners);
        binding.recyclerWinners.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerWinners.setAdapter(adapter);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null) {
            Toast.makeText(this, "Error: No event selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadWinners(eventId);
    }

    private void loadWinners(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        binding.tvMessage.setText("Event not found.");
                        binding.tvMessage.setVisibility(View.VISIBLE);
                        return;
                    }

                    Event event = snapshot.toObject(Event.class);
                    if (event == null || event.getChosenUserIds() == null || event.getChosenUserIds().isEmpty()) {
                        binding.tvMessage.setText("No lottery winners selected yet.");
                        binding.tvMessage.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> winnerIds = event.getChosenUserIds();
                    winners.clear();

                    for (String uid : winnerIds) {
                        db.collection("users").document(uid)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    UserProfile p = doc.toObject(UserProfile.class);
                                    if (p != null) {
                                        winners.add(p);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }
}
