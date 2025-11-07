package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ballerevents.databinding.EntrantMainBinding;

public class MainActivity extends AppCompatActivity {

    private EntrantMainBinding binding;
    private TrendingEventAdapter trendingAdapter;
    private NearEventAdapter nearAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupTrendingRecyclerView();
        setupNearRecyclerView();

        // --- NEW: Add click listener for Menu Button ---
        binding.btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupTrendingRecyclerView() {
        // Create the click handler using a lambda
        trendingAdapter = new TrendingEventAdapter(event -> launchDetailsActivity(event));

        binding.rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvTrending.setAdapter(trendingAdapter);

        // Load data
        trendingAdapter.submitList(EventRepository.getTrendingEvents());
    }

    private void setupNearRecyclerView() {
        // Create the click handler
        nearAdapter = new NearEventAdapter(event -> launchDetailsActivity(event));

        binding.rvNearYou.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvNearYou.setAdapter(nearAdapter);

        // Load data
        nearAdapter.submitList(EventRepository.getEventsNearYou());
    }

    private void launchDetailsActivity(Event event) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, event.getId());
        startActivity(intent);
    }
}