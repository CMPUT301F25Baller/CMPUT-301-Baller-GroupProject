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

/**
 * Activity that displays detailed information for a single user profile.
 * <p>
 * Primary usage:
 * <ul>
 *     <li>Load profile data from Firestore using {@link #EXTRA_PROFILE_ID}</li>
 *     <li>Fallback to legacy extras when no profile ID is provided</li>
 *     <li>Allow the caller to "delete" (remove) a user from a list via result intent</li>
 *     <li>Prototype messaging entry point</li>
 * </ul>
 * <p>
 * Layout is accessed via {@link ActivityProfileDetailsBinding}.
 * </p>
 */
public class ProfileDetailsActivity extends AppCompatActivity {

    /** Intent extra key for passing a Firestore user document ID. */
    public static final String EXTRA_PROFILE_ID = "extra_profile_id";

    /** Legacy extra for passing a profile display name directly. */
    public static final String EXTRA_PROFILE_NAME = "extra_profile_name";

    /** Legacy extra for passing an avatar drawable resource ID. */
    public static final String EXTRA_PROFILE_AVATAR_RES = "extra_profile_avatar_res";

    /** Legacy extra for passing a profile bio string. */
    public static final String EXTRA_PROFILE_BIO = "extra_profile_bio";

    /** Legacy extra for passing a follower count value. */
    public static final String EXTRA_PROFILE_FOLLOWERS = "extra_profile_followers";

    /** Legacy extra for passing a following count value. */
    public static final String EXTRA_PROFILE_FOLLOWING = "extra_profile_following";

    /** ViewBinding for the profile details layout. */
    private ActivityProfileDetailsBinding binding;

    /** Firestore instance used to load profile data when an ID is provided. */
    private FirebaseFirestore db;

    /** Firestore document ID of the profile being displayed (if available). */
    private String profileId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.topAppBar);
        binding.topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        binding.topAppBar.setNavigationOnClickListener(
                v -> getOnBackPressedDispatcher().onBackPressed()
        );

        // Handle system back press with a simple finish()
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        Intent i = getIntent();
        profileId = i.getStringExtra(EXTRA_PROFILE_ID);

        // Prefer Firestore ID, fallback to legacy extras if not provided
        if (profileId != null && !profileId.isEmpty()) {
            loadFromFirestore(profileId);
        } else {
            bindFromIntentFallback(i);
        }

        // "Delete user" returns the profile ID to the caller
        binding.btnDeleteUser.setOnClickListener(v ->
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
                        .show()
        );

        // Prototype messaging entry point
        binding.btnMessage.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setMessage("Messaging coming soon.")
                        .setPositiveButton("OK", null)
                        .show()
        );
    }

    /**
     * Loads the profile data for a given Firestore user document ID and binds it to the UI.
     * <p>
     * If the document does not exist or is invalid, the activity displays a Toast
     * and finishes.
     * </p>
     *
     * @param userId Firestore document ID of the user to load.
     */
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

                    // Name
                    binding.tvName.setText(nullTo(up.getName(), "User"));

                    // Bio
                    binding.tvBio.setText(nullTo(
                            up.getAboutMe(),
                            "This user hasn’t written a bio yet."
                    ));

                    // Followers/Following – default to 0 until schema supports lists/counts
                    Integer followers = 0;
                    Integer following = 0;
                    // Example once schema is extended:
                    // List<String> followersIds = up.getFollowersIds();
                    // List<String> followingIds = up.getFollowingIds();
                    // followers = followersIds != null ? followersIds.size() : 0;
                    // following = followingIds != null ? followingIds.size() : 0;

                    binding.tvFollowersCount.setText(String.valueOf(followers));
                    binding.tvFollowingCount.setText(String.valueOf(following));

                    // Avatar (URL from profile)
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

    /**
     * Binds profile details using legacy intent extras when no Firestore ID is provided.
     * <p>
     * This supports older callers that directly pass display data instead of a
     * document ID. All values have safe fallbacks to prevent empty UI.
     * </p>
     *
     * @param i The launching {@link Intent} containing legacy extras.
     */
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

    /**
     * Returns {@code fallback} if the input string is {@code null} or empty,
     * otherwise returns the original string.
     *
     * @param s        Input string (may be null or empty).
     * @param fallback Fallback value to use if {@code s} is null/empty.
     * @return {@code s} when non-null and non-empty, otherwise {@code fallback}.
     */
    private static String nullTo(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }
}
