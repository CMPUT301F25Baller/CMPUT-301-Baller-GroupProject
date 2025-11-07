package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;

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

        // === RecyclerView setups ===
        binding.rvEvents.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvProfiles.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // === Adapters ===
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

        // === Load mock data from StubAdminRepository ===
        repo.getRecentEvents(eventsAdapter::submitList);
        repo.getRecentProfiles(profilesAdapter::submitList);
        repo.getRecentImages(postersAdapter::submitList);

        // === Navigation: open the Admin Events list screen ===
        binding.chipEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventsActivity.class)));

        binding.btnSeeAllEvents.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminEventsActivity.class)));

        // === Keep these as stubs until their pages exist ===
        binding.chipPeople.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminProfilesActivity.class));
        });

        binding.chipImages.setOnClickListener(v -> {
            // TODO: Open Admin Images list
        });
        binding.chipLogs.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationLogsActivity.class)));


        binding.btnSeeAllProfiles.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminProfilesActivity.class)));

        binding.btnSeeAllImages.setOnClickListener(v -> {
            // TODO: Open full images list
        });
    }
}
