package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ballerevents.databinding.ActivityProfileDetailsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE_ID = "extra_profile_id";
    public static final String EXTRA_PROFILE_NAME = "extra_profile_name";
    public static final String EXTRA_PROFILE_AVATAR_RES = "extra_profile_avatar_res";
    public static final String EXTRA_PROFILE_BIO = "extra_profile_bio";
    public static final String EXTRA_PROFILE_FOLLOWERS = "extra_profile_followers";
    public static final String EXTRA_PROFILE_FOLLOWING = "extra_profile_following";

    private ActivityProfileDetailsBinding binding;
    private String profileId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar: back + (optional) overflow menu later if you want
        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        binding.topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        // Also handle system back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finish(); }
        });

        Intent i = getIntent();
        profileId = i.getStringExtra(EXTRA_PROFILE_ID);
        String name = i.getStringExtra(EXTRA_PROFILE_NAME);
        int avatarRes = i.getIntExtra(EXTRA_PROFILE_AVATAR_RES, 0);
        String bio = i.getStringExtra(EXTRA_PROFILE_BIO);
        int followers = i.getIntExtra(EXTRA_PROFILE_FOLLOWERS, 0);
        int following = i.getIntExtra(EXTRA_PROFILE_FOLLOWING, 0);

        // Bind UI
        TextView tvName = binding.tvName;
        tvName.setText(name != null ? name : "User");

        ImageView ivAvatar = binding.ivAvatar;
        if (avatarRes != 0) ivAvatar.setImageResource(avatarRes);

        binding.tvBio.setText(bio != null ? bio
                : "This user hasn’t written a bio yet.");

        binding.tvFollowersCount.setText(String.valueOf(followers));
        binding.tvFollowingCount.setText(String.valueOf(following));

        // Delete user button -> confirm dialog -> return result to caller
        binding.btnDeleteUser.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete user?")
                    .setMessage("This removes the user from your current list view. You can re-add them later from your data source.")
                    .setPositiveButton("Delete", (d, which) -> {
                        Intent result = new Intent();
                        result.putExtra(EXTRA_PROFILE_ID, profileId);
                        setResult(RESULT_OK, result);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // “Message” button stub (optional)
        binding.btnMessage.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setMessage("Messaging coming soon.")
                        .setPositiveButton("OK", null)
                        .show()
        );
    }
}
