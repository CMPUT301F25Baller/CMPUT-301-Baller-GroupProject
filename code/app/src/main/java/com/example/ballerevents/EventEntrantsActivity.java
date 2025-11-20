package com.example.ballerevents;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EventEntrantsActivity extends AppCompatActivity {
    long eventId = 1L; // TODO: later pass from Event screen
    private Chip chipFilter;
    private MaterialButton inviteButton;
    private ImageButton backButton;

    private RecyclerView recyclerView;
    String filter = "All";


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_entrants);

        chipFilter = findViewById(R.id.chipFilter);
        setupChipFilter();

        inviteButton = findViewById(R.id.inviteButton);
        inviteButton.setOnClickListener(v -> {
            Toast.makeText(this, "Invite button clicked", Toast.LENGTH_SHORT).show();
            // Empty for now, will add functionality later
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Toast.makeText(this, "Back button clicked", Toast.LENGTH_SHORT).show();
            // Empty for now, will add functionality later
        });

        recyclerView = findViewById(R.id.recyclerEntrants);
        loadEntrants();
    }

    private void setupChipFilter(){
        chipFilter.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(EventEntrantsActivity.this, chipFilter);
            menu.getMenu().add("All");
            menu.getMenu().add("Enrolled");
            menu.getMenu().add("Waitlisted");
            menu.getMenu().add("Invited");
            menu.getMenu().add("Declined");
            menu.getMenu().add("Cancelled");

            menu.setOnMenuItemClickListener(item -> {
                filter = Objects.requireNonNull(item.getTitle()).toString();
                chipFilter.setText(filter);
                loadEntrants();
                return true;
            });

            menu.show();
        });
    }
    private void loadEntrants(){
        List<Entrant> list = EntrantRepository.getEntrantsByStatus(eventId, filter);
        recyclerView.setAdapter(new EntrantAdapter(list));
    }
}

