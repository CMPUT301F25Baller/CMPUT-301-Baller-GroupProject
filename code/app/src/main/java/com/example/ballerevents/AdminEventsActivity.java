package com.example.ballerevents;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminEventsActivity extends AppCompatActivity implements AdminEventsAdapter.OnEventActionListener {

    private AdminEventsAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_events);

        adapter = new AdminEventsAdapter(this);

        androidx.recyclerview.widget.RecyclerView rvEvents = findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(adapter);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle("Events");
        topAppBar.setNavigationIcon(R.drawable.ic_arrow_left);
        topAppBar.setNavigationOnClickListener(v -> finish());

        TextInputEditText etSearch = findViewById(R.id.etSearch);

        // ✅ Load from the same mock source as Admin Dashboard
        List<Event> repoList = EventRepository.getTrendingEvents();
        allEvents = (repoList != null) ? repoList : new ArrayList<>();
        adapter.setEvents(allEvents);

        // Search filter (title)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        if (query == null) query = "";
        String q = query.toLowerCase();
        List<Event> filtered = new ArrayList<>();
        for (Event e : allEvents) {
            String t = e.getTitle() == null ? "" : e.getTitle();
            if (t.toLowerCase().contains(q)) filtered.add(e);
        }
        adapter.setEvents(filtered);
    }

    // No repository delete; just remove locally for now
    @Override
    public void onDelete(Event event) {
        allEvents.remove(event);
        adapter.setEvents(new ArrayList<>(allEvents));
    }
}
