package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.AdminDashboardBinding;

public class AdminDashboardActivity extends AppCompatActivity {

    private AdminDashboardBinding binding;
    private final AdminRepository repo = new StubAdminRepository();

    private TrendingEventAdapter eventsAdapter;
    private AdminProfilesAdapter profilesAdapter;
    private AdminPostersAdapter postersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Horizontal lists
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvProfiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Adapters
        eventsAdapter = new TrendingEventAdapter(event -> {
            Intent i = new Intent(this, DetailsActivity.class);
            i.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
            startActivity(i);
        });
        profilesAdapter = new AdminProfilesAdapter();
        postersAdapter = new AdminPostersAdapter();

        binding.rvEvents.setAdapter(eventsAdapter);
        binding.rvProfiles.setAdapter(profilesAdapter);
        binding.rvImages.setAdapter(postersAdapter);

        // Load data from stub “database”
        repo.getRecentEvents(events -> eventsAdapter.submitList(events));
        repo.getRecentProfiles(profiles -> profilesAdapter.submitList(profiles));
        repo.getRecentImages(images -> postersAdapter.submitList(images));

        // Chips & See all (still stubs)
        binding.chipEvents.setOnClickListener(v -> Toast.makeText(this, "Open: Admin Events list", Toast.LENGTH_SHORT).show());
        binding.chipPeople.setOnClickListener(v -> Toast.makeText(this, "Open: Admin Profiles list", Toast.LENGTH_SHORT).show());
        binding.chipImages.setOnClickListener(v -> Toast.makeText(this, "Open: Admin Images list", Toast.LENGTH_SHORT).show());
        binding.chipLogs.setOnClickListener(v -> Toast.makeText(this, "Open: Notification Logs (later)", Toast.LENGTH_SHORT).show());

        binding.btnSeeAllEvents.setOnClickListener(v -> Toast.makeText(this, "See all events", Toast.LENGTH_SHORT).show());
        binding.btnSeeAllProfiles.setOnClickListener(v -> Toast.makeText(this, "See all profiles", Toast.LENGTH_SHORT).show());
        binding.btnSeeAllImages.setOnClickListener(v -> Toast.makeText(this, "See all images", Toast.LENGTH_SHORT).show());
    }
}
