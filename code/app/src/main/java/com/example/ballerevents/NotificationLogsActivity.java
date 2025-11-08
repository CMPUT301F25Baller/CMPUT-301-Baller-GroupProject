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

public class NotificationLogsActivity extends AppCompatActivity {

    private ActivityNotificationLogsBinding binding;
    private SimpleTextAdapter adapter;
    private final List<String> allItems = new ArrayList<>();
    private final List<String> newItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        allItems.addAll(Arrays.asList(
                "David Silbia invited you • Just now",
                "Adnan Safi added you to waitlist • 5 min ago",
                "Joan Baker – Smooth Jazz • 20 min ago",
                "Ronald C. Kinch added you • 1 hr ago",
                "Clara Tolson invited you • 9 hr ago",
                "Eric G. Prickett sent an invitation • Wed, 3:30 pm",
                "Jennifer Fritz – Event cancelled • Tue, 5:10 pm"
        ));
        newItems.addAll(allItems.subList(0, Math.min(2, allItems.size())));

        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleTextAdapter(allItems);
        binding.rvLogs.setAdapter(adapter);

        Chip chipNew = binding.chipNew;
        Chip chipAll = binding.chipAll;
        chipAll.setChecked(true);

        chipNew.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                adapter = new SimpleTextAdapter(new ArrayList<>(newItems));
                binding.rvLogs.setAdapter(adapter);
            }
        });
        chipAll.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                adapter = new SimpleTextAdapter(new ArrayList<>(allItems));
                binding.rvLogs.setAdapter(adapter);
            }
        });

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
