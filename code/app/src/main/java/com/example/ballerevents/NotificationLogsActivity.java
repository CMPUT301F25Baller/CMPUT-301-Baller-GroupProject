package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Activity displaying a list of notification logs to the user.
 * <p>
 * The screen contains a RecyclerView showing log entries in text form,
 * along with chips that allow switching between:
 * <ul>
 *     <li>Only new/unread items</li>
 *     <li>All notification logs</li>
 * </ul>
 * A "Mark All" button allows marking all notifications as read and updates
 * the RecyclerView accordingly.
 * </p>
 *
 * <p>This activity uses {@link SimpleTextAdapter} to render the text rows.</p>
 */
public class NotificationLogsActivity extends AppCompatActivity {

    /** ViewBinding for accessing the notification logs layout. */
    private ActivityNotificationLogsBinding binding;

    /** RecyclerView adapter for displaying notification text rows. */
    private SimpleTextAdapter adapter;

    /** Full list of mock notification log entries. */
    private final List<String> allItems = new ArrayList<>();

    /** Subset of the newest/unread items. */
    private final List<String> newItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Mock notification data for prototype UI
        allItems.addAll(Arrays.asList(
                "David Silbia invited you • Just now",
                "Adnan Safi added you to waitlist • 5 min ago",
                "Joan Baker – Smooth Jazz • 20 min ago",
                "Ronald C. Kinch added you • 1 hr ago",
                "Clara Tolson invited you • 9 hr ago",
                "Eric G. Prickett sent an invitation • Wed, 3:30 pm",
                "Jennifer Fritz – Event cancelled • Tue, 5:10 pm"
        ));

        // First 1–2 messages treated as new items
        newItems.addAll(allItems.subList(0, Math.min(2, allItems.size())));

        // RecyclerView setup
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleTextAdapter(allItems);
        binding.rvLogs.setAdapter(adapter);

        Chip chipNew = binding.chipNew;
        Chip chipAll = binding.chipAll;
        chipAll.setChecked(true);

        // Chip: show only new/unread logs
        chipNew.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                adapter = new SimpleTextAdapter(new ArrayList<>(newItems));
                binding.rvLogs.setAdapter(adapter);
            }
        });

        // Chip: show all logs
        chipAll.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                adapter = new SimpleTextAdapter(new ArrayList<>(allItems));
                binding.rvLogs.setAdapter(adapter);
            }
        });

        // "Mark all as read" button
        binding.btnMarkAll.setOnClickListener(v -> {
            newItems.clear();
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();

            // If the "New" filter is active, refresh to show empty list
            if (chipNew.isChecked()) {
                adapter = new SimpleTextAdapter(Collections.emptyList());
                binding.rvLogs.setAdapter(adapter);
            }
        });
    }
}
