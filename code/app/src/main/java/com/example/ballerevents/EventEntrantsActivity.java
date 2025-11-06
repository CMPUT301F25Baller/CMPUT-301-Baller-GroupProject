package com.example.ballerevents;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

public class EventEntrantsActivity extends AppCompatActivity {
    private Chip chipFilter;
    private MaterialButton inviteButton;
    private ImageButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_entrants);

        chipFilter = findViewById(R.id.chipFilter);
        setupChipFilter();

        inviteButton = findViewById(R.id.inviteButton);
        inviteButton.setOnClickListener(v -> {
            // Empty for now, will add functionality later
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Empty for now, will add functionality later
        });
    }

    private void setupChipFilter(){
        chipFilter.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(EventEntrantsActivity.this, chipFilter);
            menu.getMenu().add("Enrolled");
            menu.getMenu().add("Invited");
            menu.getMenu().add("Waitlist");

            menu.setOnMenuItemClickListener(item -> {
                chipFilter.setText(item.getTitle());
                return true;
            });

            menu.show();
        });
    }
}
