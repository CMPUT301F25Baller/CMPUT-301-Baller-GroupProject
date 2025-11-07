package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ballerevents.databinding.EntrantNotificationsEmptyBinding;

public class NotificationsActivity extends AppCompatActivity {

    private EntrantNotificationsEmptyBinding binding;
    private final NotificationsRepository repo = new StubNotificationsRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EntrantNotificationsEmptyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back arrow (only if you set a navigationIcon in the toolbar)
        binding.topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // Load (always empty for now)
        repo.getMyNotifications(items -> {
            // if you later add a RecyclerView, toggle visibility here.
            binding.emptyContainer.setVisibility(View.VISIBLE);
        });

        binding.btnBrowse.setOnClickListener(v -> {
            // Go back to main list (or Admin dashboard—your call)
            startActivity(new Intent(this, MainActivity.class));
        });
    }
}
