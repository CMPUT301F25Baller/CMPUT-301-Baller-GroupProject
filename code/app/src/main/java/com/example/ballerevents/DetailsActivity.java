package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ballerevents.databinding.EntrantEventDetailsBinding;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";

    private EntrantEventDetailsBinding binding;
    private Event mEvent;
    private boolean mIsUserApplied;
    private int mCurrentWaitlistCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1L);
        if (eventId == -1L) {
            Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mEvent = EventRepository.getEventById(eventId);
        if (mEvent == null) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get dynamic data from the repository
        mIsUserApplied = EventRepository.isUserApplied(mEvent.getId(), EventRepository.MOCK_USER_ID);
        mCurrentWaitlistCount = EventRepository.getDynamicWaitlistCount(mEvent.getId());

        populateStaticUi(mEvent);
        updateButtonAndCountUI();

        // Set up click listeners
        binding.btnJoinWaitlist.setOnClickListener(v -> handleWaitlistClick());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Populates the UI with static event data (title, description, etc.)
     */
    private void populateStaticUi(Event event) {
        binding.ivEventBanner.setImageResource(event.getEventPosterResId());
        binding.tvEventTitle.setText(event.getTitle());
        binding.tvEventDate.setText(event.getDate());
        binding.tvEventTime.setText(event.getTime());
        binding.tvEventLocationName.setText(event.getLocationName());
        binding.tvEventLocationAddress.setText(event.getLocationAddress());
        binding.ivOrganizer.setImageResource(event.getOrganizerIconResId());
        binding.tvOrganizerName.setText(event.getOrganizer());
        binding.tvAboutEventDescription.setText(event.getDescription());
    }

    /**
     * Updates the button text and waitlist count based on the user's application status.
     */
    private void updateButtonAndCountUI() {
        // Update the count text
        binding.tvWaitlistCount.setText("+" + mCurrentWaitlistCount + " On Waitlist");

        // Update the button
        if (mIsUserApplied) {
            binding.btnJoinWaitlist.setText("Withdraw");
            // You could also change the button color here
        } else {
            binding.btnJoinWaitlist.setText("Join Waitlist");
            // You could change the color back here
        }
        // Re-enable the button in case it was disabled by a previous click
        binding.btnJoinWaitlist.setEnabled(true);
    }

    /**
     * Handles the logic for joining or withdrawing from the waitlist.
     */
    private void handleWaitlistClick() {
        // Disable button briefly to prevent double-clicks
        binding.btnJoinWaitlist.setEnabled(false);

        if (mIsUserApplied) {
            // User wants to WITHDRAW
            EventRepository.withdrawFromEvent(mEvent.getId(), EventRepository.MOCK_USER_ID);
            mIsUserApplied = false;
            mCurrentWaitlistCount--; // Decrement count
            Toast.makeText(this, "Withdrawn from " + mEvent.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            // User wants to APPLY
            EventRepository.applyToEvent(mEvent.getId(), EventRepository.MOCK_USER_ID);
            mIsUserApplied = true;
            mCurrentWaitlistCount++; // Increment count
            Toast.makeText(this, "Applied to lottery for " + mEvent.getTitle(), Toast.LENGTH_SHORT).show();
        }

        // Refresh the UI to show the new state
        updateButtonAndCountUI();
    }
}