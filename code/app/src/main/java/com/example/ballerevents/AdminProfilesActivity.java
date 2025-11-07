package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ballerevents.databinding.ActivityAdminProfilesBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProfilesActivity extends AppCompatActivity {

    private ActivityAdminProfilesBinding binding;
    private final AdminRepository repo = new StubAdminRepository();

    private ProfilesListAdapter adapter;
    private final List<Profile> data = new ArrayList<>();

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String deletedId = result.getData().getStringExtra(ProfileDetailsActivity.EXTRA_PROFILE_ID);
                    if (deletedId != null) {
                        int idx = findIndexById(deletedId);
                        if (idx >= 0) {
                            data.remove(idx);
                            adapter.submitList(new ArrayList<>(data));
                        }
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProfilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        binding.topAppBar.setTitle("Profiles");
        binding.topAppBar.setNavigationOnClickListener(v -> finish());

        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProfilesListAdapter(this::openDetails);
        binding.recycler.setAdapter(adapter);

        // Load mock data from stub repository
        binding.progress.setVisibility(View.VISIBLE);
        repo.getRecentProfiles(list -> {
            binding.progress.setVisibility(View.GONE);
            data.clear();
            data.addAll(list);
            adapter.submitList(new ArrayList<>(data));
        });
    }

    private void openDetails(Profile p) {
        Intent i = new Intent(this, ProfileDetailsActivity.class);
        // Direct field access instead of getters
        i.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_ID, p.id);
        i.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_NAME, p.name != null ? p.name : p.id);
        i.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_AVATAR_RES, p.avatarResId);
        i.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_BIO, "");
        i.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_FOLLOWERS, 346);
        i.putExtra(ProfileDetailsActivity.EXTRA_PROFILE_FOLLOWING, 350);

        detailLauncher.launch(i);
    }

    private int findIndexById(String id) {
        for (int i = 0; i < data.size(); i++) {
            if (id.equals(data.get(i).id)) return i;
        }
        return -1;
    }
}
