package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity displayed immediately after a successful login where the user selects
 * their primary role in the app.
 * <p>
 * The selected role is written to the user's Firestore document (field
 * {@code "role"} in the {@code users} collection) and the user is then
 * navigated to the appropriate main activity.
 * <p>
 * Update: Both Entrants and Organizers are now routed to {@link EntrantMainActivity}
 * to provide a unified dashboard experience.
 */
public class RoleSelectionActivity extends AppCompatActivity {

    /** Logging tag for role selection flow. */
    private static final String TAG = "RoleSelectionActivity";

    /** Firestore instance used to update the user's role. */
    private FirebaseFirestore db;

    /** FirebaseAuth instance for retrieving the current user ID. */
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // If no authenticated user is present, return to the login screen.
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Button btnOrganizer = findViewById(R.id.btnOrganizer);
        Button btnEntrant = findViewById(R.id.btnEntrant);
        Button btnAdmin = findViewById(R.id.btnAdmin);

        // Update: Organizers now go to the main dashboard (EntrantMainActivity) first
        btnOrganizer.setOnClickListener(v ->
                selectRole("organizer", EntrantMainActivity.class)
        );

        btnEntrant.setOnClickListener(v ->
                selectRole("entrant", EntrantMainActivity.class)
        );

        btnAdmin.setOnClickListener(v ->
                selectRole("admin", AdminMainActivity.class)
        );
    }

    /**
     * Persists the chosen role in the current user's Firestore document and
     * then starts the given activity.
     *
     * @param role          Role string to be stored (e.g., {@code "entrant"}, {@code "organizer"}, {@code "admin"}).
     * @param activityClass Target activity class to navigate to after the role is set.
     */
    private void selectRole(String role, Class<?> activityClass) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("role", role)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User role set to: " + role);
                    startActivity(new Intent(RoleSelectionActivity.this, activityClass));
                    // Prevent navigating back to role selection
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error setting role", e);
                    Toast.makeText(this, "Error setting role.", Toast.LENGTH_SHORT).show();
                });
    }
}