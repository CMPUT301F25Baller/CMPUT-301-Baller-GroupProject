package com.example.ballerevents;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrganizerProfileActivity extends AppCompatActivity {

    private TextView tabAbout, tabEvent, tabFollowing;
    private ScrollView sectionAbout;
    private ListView sectionEvent, sectionFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_profile);

        // Tabs
        tabAbout = findViewById(R.id.tabAbout);
        tabEvent = findViewById(R.id.tabEvent);
        tabFollowing = findViewById(R.id.tabFollowing);

        // Sections
        sectionAbout = findViewById(R.id.sectionAbout);
        sectionEvent = findViewById(R.id.sectionEvent);
        sectionFollowing = findViewById(R.id.sectionFollowing);

        // Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        // Fill EVENT list with A/B/C/D
        String[] events = { "Event A", "Event B", "Event C", "Event D" };
        ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                events
        );
        sectionEvent.setAdapter(eventAdapter);

        // Fill FOLLOWING list with Entrant A/B/C/D
        String[] entrants = {
                "Entrant A - Started following you",
                "Entrant B - Joined waitlist",
                "Entrant C - Joined waitlist",
                "Entrant D - Started following you"
        };
        ArrayAdapter<String> followingAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                entrants
        );
        sectionFollowing.setAdapter(followingAdapter);

        // Tab click listeners
        tabAbout.setOnClickListener(v -> showAbout());
        tabEvent.setOnClickListener(v -> showEvent());
        tabFollowing.setOnClickListener(v -> showFollowing());

        // Default
        showAbout();
    }

    private void showAbout() {
        sectionAbout.setVisibility(ScrollView.VISIBLE);
        sectionEvent.setVisibility(ListView.GONE);
        sectionFollowing.setVisibility(ListView.GONE);

        setActiveTab(tabAbout, tabEvent, tabFollowing);
    }

    private void showEvent() {
        sectionAbout.setVisibility(ScrollView.GONE);
        sectionEvent.setVisibility(ListView.VISIBLE);
        sectionFollowing.setVisibility(ListView.GONE);

        setActiveTab(tabEvent, tabAbout, tabFollowing);
    }

    private void showFollowing() {
        sectionAbout.setVisibility(ScrollView.GONE);
        sectionEvent.setVisibility(ListView.GONE);
        sectionFollowing.setVisibility(ListView.VISIBLE);

        setActiveTab(tabFollowing, tabAbout, tabEvent);
    }

    private void setActiveTab(TextView active, TextView other1, TextView other2) {
        int activeColor = 0xFF5669FF;
        int inactiveColor = 0xFF747688;

        active.setTextColor(activeColor);
        other1.setTextColor(inactiveColor);
        other2.setTextColor(inactiveColor);
    }
}
