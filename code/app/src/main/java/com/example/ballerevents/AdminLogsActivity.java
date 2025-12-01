package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Activity that displays a simple, prototype notification log for the admin.
 *
 * <p>This screen serves as a placeholder UI, showing log messages in a list.
 * The data is currently mocked in-memory and demonstrates:
 *
 * <ul>
 * <li>Switching between “New” and “All” logs using Material Chips</li>
 * <li>Simple list rendering via {@link SimpleTextAdapter}</li>
 * <li>“Mark all as read” button behavior</li>
 * </ul>
 *
 * <p>Updated to support the shared custom header layout used by NotificationLogsActivity.</p>
 */
public class AdminLogsActivity extends AppCompatActivity {

    /** View binding for activity_notification_logs.xml */
    private ActivityNotificationLogsBinding binding;

    /** Adapter used to render the logs in the RecyclerView */
    private SimpleTextAdapter adapter;

    /** In-memory list of all notifications (mock data). */
    private final List<String> allItems = new ArrayList<>();

    /** Subset representing unread or recently added notifications. */
    private final List<String> newItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- NEW: Handle Custom Back Button ---
        // Replaces the old setupToolbar() logic
        if (binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }

        initializeMockData();
        setupRecycler();
        setupChipsAndActions();
    }

    /**
     * Populates in-memory mock notification data.
     * In the real implementation, this will be pulled from Firestore.
     */
    private void initializeMockData() {
        allItems.addAll(Arrays.asList(
                "David Silbia invited you to Jo Malone London’s Mother’s… • Just now",
                "Adnan Safi added you to waitlist • 5 min ago",
                "Joan Baker – Virtual Evening of Smooth Jazz • 20 min ago",
                "Ronald C. Kinch added you to waitlist • 1 hr ago",
                "Clara Tolson invited you to Gala Music Festival • 9 hr ago",
                "Eric G. Prickett sent an invitation • Wed, 3:30 pm",
                "Jennifer Fritz – Kids Safe Event cancelled • Tue, 5:10 pm"
        ));

        // Mock “new” notifications as the first two items
        newItems.addAll(allItems.subList(0, Math.min(2, allItems.size())));
    }

    /**
     * Sets up the RecyclerView and assigns the default adapter.
     */
    private void setupRecycler() {
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleTextAdapter(allItems);
        binding.rvLogs.setAdapter(adapter);
    }

    /**
     * Wires up chip selection logic and the “Mark all as read” button.
     */
    private void setupChipsAndActions() {
        Chip chipNew = binding.chipNew;
        Chip chipAll = binding.chipAll;

        chipAll.setChecked(true); // Default filter

        chipNew.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                adapter = new SimpleTextAdapter(new ArrayList<>(newItems));
                binding.rvLogs.setAdapter(adapter);
            }
        });

        chipAll.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                adapter = new SimpleTextAdapter(new ArrayList<>(allItems));
                binding.rvLogs.setAdapter(adapter);
            }
        });

        binding.btnMarkAll.setOnClickListener(v -> {
            newItems.clear();
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();

            // If viewing "New", update display to empty
            if (chipNew.isChecked()) {
                adapter = new SimpleTextAdapter(Collections.emptyList());
                binding.rvLogs.setAdapter(adapter);
            }
        });
    }
}