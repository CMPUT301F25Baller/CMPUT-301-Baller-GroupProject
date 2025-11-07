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
 * Simple notification log screen used as a placeholder for admin logs.
 * Displays sample rows and allows toggling between "New" and "All" via chips.
 */


public class AdminLogsActivity extends AppCompatActivity {

    private ActivityNotificationLogsBinding binding;
    private SimpleTextAdapter adapter;
    private final List<String> allItems = new ArrayList<>();
    private final List<String> newItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar back
        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        // Sample data (replace with your repo later)
        allItems.addAll(Arrays.asList(
                "David Silbia invited you to Jo Malone London’s Mother’s… • Just now",
                "Adnan Safi added you to waitlist • 5 min ago",
                "Joan Baker – Virtual Evening of Smooth Jazz • 20 min ago",
                "Ronald C. Kinch added you to waitlist • 1 hr ago",
                "Clara Tolson invited you to Gala Music Festival • 9 hr ago",
                "Eric G. Prickett sent an invitation • Wed, 3:30 pm",
                "Jennifer Fritz – Kids Safe Event cancelled • Tue, 5:10 pm"
        ));
        // Pretend “New” is the first two
        newItems.addAll(allItems.subList(0, Math.min(2, allItems.size())));

        // Recycler
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleTextAdapter(allItems);
        binding.rvLogs.setAdapter(adapter);

        // Chips
        final Chip chipNew = binding.chipNew;
        final Chip chipAll = binding.chipAll;
        chipAll.setChecked(true);

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

        // Mark all as read
        binding.btnMarkAll.setOnClickListener(v -> {
            newItems.clear();
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
            if (chipNew.isChecked()) {
                adapter = new SimpleTextAdapter(Collections.emptyList());
                binding.rvLogs.setAdapter(adapter);
            }
        });
    }
}
