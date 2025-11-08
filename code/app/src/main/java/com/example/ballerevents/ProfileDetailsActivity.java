package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ballerevents.databinding.ActivityProfileDetailsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProfileDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE_ID = "extra_profile_id";
    // legacy extras (kept for fallback)
    public static final String EXTRA_PROFILE_NAME = "extra_profile_name";
    public static final String EXTRA_PROFILE_AVATAR_RES = "extra_profile_avatar_res";
    public static final String EXTRA_PROFILE_BIO = "extra_profile_bio";
    public static final String EXTRA_PROFILE_FOLLOWERS = "extra_profile_followers";
    public static final String EXTRA_PROFILE_FOLLOWING = "extra_profile_following";

    private ActivityProfileDetailsBinding binding;
    private FirebaseFirestore db;
    private String profileId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        binding.topAppBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finish(); }
        });

        Intent i = getIntent();
        profileId = i.getStringExtra(EXTRA_PROFILE_ID);

        if (profileId != null && !profileId.isEmpty()) {
            loadFromFirestore(profileId);
        } else {
            // Fallback: support old callers passing data directly
            bindFromIntentFallback(i);
        }

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

        binding.btnMessage.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setMessage("Messaging coming soon.")
                        .setPositiveButton("OK", null)
                        .show()
        );
    }

    private void loadFromFirestore(String userId) {
        DocumentReference ref = db.collection("users").document(userId);
        ref.get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    UserProfile up = doc.toObject(UserProfile.class);
                    if (up == null) {
                        Toast.makeText(this, "Invalid user data", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    // name
                    binding.tvName.setText(nullTo(up.getName(), "User"));
                    // bio
                    binding.tvBio.setText(nullTo(up.getAboutMe(), "This user hasn’t written a bio yet."));
                    // followers/following — if you don’t have these in schema yet, show 0
                    Integer followers = 0;
                    Integer following = 0;
                    // If you later add fields, e.g. List<String> followersIds / followingIds:
                    // List<String> followersIds = up.getFollowersIds();
                    // List<String> followingIds = up.getFollowingIds();
                    // followers = followersIds != null ? followersIds.size() : 0;
                    // following = followingIds != null ? followingIds.size() : 0;
                    binding.tvFollowersCount.setText(String.valueOf(followers));
                    binding.tvFollowingCount.setText(String.valueOf(following));

                    // avatar (URL)
                    Glide.with(this)
                            .load(up.getProfilePictureUrl())
                            .placeholder(R.drawable.placeholder_avatar1)
                            .error(R.drawable.placeholder_avatar1)
                            .into(binding.ivAvatar);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void bindFromIntentFallback(Intent i) {
        String name = i.getStringExtra(EXTRA_PROFILE_NAME);
        int avatarRes = i.getIntExtra(EXTRA_PROFILE_AVATAR_RES, 0);
        String bio = i.getStringExtra(EXTRA_PROFILE_BIO);
        int followers = i.getIntExtra(EXTRA_PROFILE_FOLLOWERS, 0);
        int following = i.getIntExtra(EXTRA_PROFILE_FOLLOWING, 0);

        binding.tvName.setText(name != null ? name : "User");
        binding.tvBio.setText(bio != null ? bio : "This user hasn’t written a bio yet.");
        binding.tvFollowersCount.setText(String.valueOf(followers));
        binding.tvFollowingCount.setText(String.valueOf(following));

        if (avatarRes != 0) {
            binding.ivAvatar.setImageResource(avatarRes);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.placeholder_avatar1);
        }
    }

    private static String nullTo(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }
}
