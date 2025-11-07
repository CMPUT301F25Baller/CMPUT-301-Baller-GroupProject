package com.example.ballerevents;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import java.util.ArrayList;
import java.util.List;

public class NotificationLogsActivity extends AppCompatActivity {

    private ActivityNotificationLogsBinding binding;
    private NotificationLogsAdapter adapter;
    private final List<NotificationLog> data = new ArrayList<>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        binding.topAppBar.setTitle("Notifications");
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationLogsAdapter(new NotificationLogsAdapter.OnItemAction() {
            @Override public void onMarkRead(NotificationLog log) {
                NotificationLogsStore.markRead(log.id);
                refresh();
            }
            @Override public void onOpen(NotificationLog log) {
                Toast.makeText(NotificationLogsActivity.this,
                        "Open: " + log.title, Toast.LENGTH_SHORT).show();
            }
        });
        binding.recycler.setAdapter(adapter);

        binding.btnMarkAllRead.setOnClickListener(v -> {
            NotificationLogsStore.markAllRead();
            refresh();
        });

        load();
    }

    private void load() {
        binding.progress.setVisibility(View.VISIBLE);
        List<NotificationLog> list = NotificationLogsStore.getAll();
        binding.progress.setVisibility(View.GONE);
        data.clear();
        data.addAll(list);
        adapter.submitList(new ArrayList<>(data));
    }

    private void refresh() {
        // re-pull from store so DiffUtil updates unread dots
        List<NotificationLog> list = NotificationLogsStore.getAll();
        data.clear();
        data.addAll(list);
        adapter.submitList(new ArrayList<>(data));
    }
}
