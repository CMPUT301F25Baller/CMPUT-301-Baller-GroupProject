package com.example.ballerevents;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityNotificationLogsBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class NotificationLogsActivity extends AppCompatActivity {

    private ActivityNotificationLogsBinding binding;
    private NotificationLogsAdapter adapter;
    private NotificationLogsStore store;

    private boolean filterOnlyNew = false;
    private ListenerRegistration activeSub;
    private DocumentSnapshot nextCursor;
    private final List<NotificationLog> buffer = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        adapter = new NotificationLogsAdapter();
        store = new NotificationLogsStore();

        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLogs.setAdapter(adapter);

        // Filter chips
        binding.chipAll.setChecked(true);
        binding.chipNew.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) { filterOnlyNew = true; reload(); }
        });
        binding.chipAll.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) { filterOnlyNew = false; reload(); }
        });

        // Mark all as read
        binding.btnMarkAll.setOnClickListener(v ->
                store.markAllAsRead()
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to mark as read", Toast.LENGTH_SHORT).show())
        );

        // Endless scroll
        binding.rvLogs.addOnScrollListener(new EndlessRecyclerListener(() -> {
            if (nextCursor != null) paginate(nextCursor);
        }));

        reload();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeSub != null) activeSub.remove();
    }

    private void reload() {
        if (activeSub != null) activeSub.remove();
        buffer.clear();
        adapter.submitList(new ArrayList<>(buffer));
        nextCursor = null;

        activeSub = store.watchAll(
                filterOnlyNew,
                null,
                30,
                new NotificationLogsStore.LogsListener() {
                    @Override
                    public void onChanged(List<NotificationLog> logs, @Nullable DocumentSnapshot cursor) {
                        nextCursor = cursor;
                        buffer.clear();
                        buffer.addAll(logs);
                        adapter.submitList(new ArrayList<>(buffer));
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(NotificationLogsActivity.this, "Error loading logs", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void paginate(DocumentSnapshot cursor) {
        ListenerRegistration tmp = store.watchAll(
                filterOnlyNew,
                cursor,
                30,
                new NotificationLogsStore.LogsListener() {
                    @Override
                    public void onChanged(List<NotificationLog> logs, @Nullable DocumentSnapshot next) {
                        nextCursor = next;
                        buffer.addAll(logs);
                        adapter.submitList(new ArrayList<>(buffer));
                    }

                    @Override
                    public void onError(Exception e) { /* ignore */ }
                }
        );
        tmp.remove();
    }
}
