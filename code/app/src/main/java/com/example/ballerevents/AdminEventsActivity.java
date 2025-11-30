package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class AdminEventsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private AdminEventsAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Uses res/layout/admin_events.xml
        setContentView(R.layout.admin_events);

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminEventsAdapter(event -> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();
    }

    private void loadEvents() {
        db.collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Event> events = snap.toObjects(Event.class);
                    adapter.submitList(events);
                    if (events.isEmpty()) {
                        Toast.makeText(this, "No events found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
