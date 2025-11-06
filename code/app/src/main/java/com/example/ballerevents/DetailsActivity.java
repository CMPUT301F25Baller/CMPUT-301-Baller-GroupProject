package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ballerevents.Event;
import com.example.ballerevents.EventRepository;
import com.example.ballerevents.databinding.EntrantEventDetailsBinding;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";

    private EntrantEventDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1L);
        if (eventId == -1L) {
            // Handle error: No ID passed
            Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Event event = EventRepository.getEventById(eventId);
        if (event == null) {
            // Handle error: Event not found
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateUi(event);
    }

    private void populateUi(Event event) {
        binding.ivEventBanner.setImageResource(event.getEventPosterResId());
        binding.tvWaitlistCount.setText("+" + event.getWaitlistCount() + " On Waitlist");
        binding.tvEventTitle.setText(event.getTitle());

        // Date & Time
        binding.tvEventDate.setText(event.getDate());
        binding.tvEventTime.setText(event.getTime());

        // Location
        binding.tvEventLocationName.setText(event.getLocationName());
        binding.tvEventLocationAddress.setText(event.getLocationAddress());

        // Organizer
        binding.ivOrganizer.setImageResource(event.getOrganizerIconResId());
        binding.tvOrganizerName.setText(event.getOrganizer());

        // About
        binding.tvAboutEventDescription.setText(event.getDescription());

        // Click listener for the lottery button
        binding.btnJoinWaitlist.setOnClickListener(v -> {
            // Handle applying to the lottery
            Toast.makeText(this, "Applied to lottery for " + event.getTitle(), Toast.LENGTH_SHORT).show();
            binding.btnJoinWaitlist.setText("Applied");
            binding.btnJoinWaitlist.setEnabled(false);
        });

        binding.btnBack.setOnClickListener(v -> {
            finish(); // Go back to the previous screen
        });
    }
}