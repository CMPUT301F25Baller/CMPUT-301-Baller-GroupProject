package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.AdminDashboardBinding;

import java.util.Arrays;

public class AdminDashboardActivity extends AppCompatActivity {

    private AdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Top bar title comes from XML, but you can still set it here if you want:
        // binding.topAppBar.setTitle("Admin Dashboard");

        // Make lists HORIZONTAL
        binding.rvEvents.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvProfiles.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // ---- STUB DATA (Half-way checkpoint, no backend required) ----
        binding.rvEvents.setAdapter(new SimpleTextAdapter(Arrays.asList(
                "Music of the Spheres", "Jazz Night", "Coding Jam", "Design Expo", "Hackathon"
        )));
        binding.rvProfiles.setAdapter(new SimpleTextAdapter(Arrays.asList(
                "Silbia", "Safi", "Baker", "Kinch", "Ayo", "Mina", "Zee", "Tobi"
        )));
        binding.rvImages.setAdapter(new SimpleTextAdapter(Arrays.asList(
                "poster_001", "poster_002", "poster_003", "poster_004", "poster_005"
        )));

        // Chips → just stub actions (toasts). You can wire navigation later.
        binding.chipEvents.setOnClickListener(v ->
                Toast.makeText(this, "Open: Admin Events list", Toast.LENGTH_SHORT).show());
        binding.chipPeople.setOnClickListener(v ->
                Toast.makeText(this, "Open: Admin Profiles list", Toast.LENGTH_SHORT).show());
        binding.chipImages.setOnClickListener(v ->
                Toast.makeText(this, "Open: Admin Images list", Toast.LENGTH_SHORT).show());
        binding.chipLogs.setOnClickListener(v ->
                Toast.makeText(this, "Open: Notification Logs (later)", Toast.LENGTH_SHORT).show());

        // Optional “See all” labels
        binding.btnSeeAllEvents.setOnClickListener(v ->
                Toast.makeText(this, "See all events", Toast.LENGTH_SHORT).show());
        binding.btnSeeAllProfiles.setOnClickListener(v ->
                Toast.makeText(this, "See all profiles", Toast.LENGTH_SHORT).show());
        binding.btnSeeAllImages.setOnClickListener(v ->
                Toast.makeText(this, "See all images", Toast.LENGTH_SHORT).show());
    }
}
