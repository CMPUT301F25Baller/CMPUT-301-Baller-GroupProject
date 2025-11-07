package com.example.ballerevents;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrganizerActivity extends AppCompatActivity {

    private static final String[] TAB_TITLES = {"About", "Event", "Following"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new OrganizerPagerAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        // Default to Event tab (index 1)
        viewPager.setCurrentItem(1, false);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])

        ).attach();
        findViewById(R.id.btnNewEvent).setOnClickListener(v ->
                startActivity(new android.content.Intent(this, OrganizerEventCreationActivity.class))
        );

        findViewById(R.id.btnMessage).setOnClickListener(v ->
                android.widget.Toast.makeText(this, "Messaging prototype coming soon.", android.widget.Toast.LENGTH_SHORT).show()
        );

    }
}
