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
 * Activity displayed after login where the user selects their primary role.
 * This selection updates their user document in Firestore and navigates them
 * to the appropriate main activity (Entrant, Organizer, or Admin).
 */
public class RoleSelectionActivity extends AppCompatActivity {

    private static final String TAG = "RoleSelectionActivity";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            // No user logged in, go back to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Button btnOrganizer = findViewById(R.id.btnOrganizer);
        Button btnEntrant = findViewById(R.id.btnEntrant);
        Button btnAdmin = findViewById(R.id.btnAdmin);

        btnOrganizer.setOnClickListener(v ->
                selectRole("organizer", OrganizerActivity.class)
        );

        btnEntrant.setOnClickListener(v ->
                selectRole("entrant", EntrantMainActivity.class)
        );

        btnAdmin.setOnClickListener(v ->
                selectRole("admin", AdminMainActivity.class) // Updated to launch AdminMainActivity
        );
    }

    /**
     * Updates the user's "role" field in their Firestore document and then
     * navigates them to the corresponding main activity.
     *
     * @param role The role string to save (e.g., "entrant", "organizer").
     * @param activityClass The .class of the Activity to navigate to.
     */
    private void selectRole(String role, Class<?> activityClass) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .update("role", role)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User role set to: " + role);
                    startActivity(new Intent(RoleSelectionActivity.this, activityClass));
                    finish(); // Optional: finish so they can't come back to role selection
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error setting role", e);
                    Toast.makeText(this, "Error setting role.", Toast.LENGTH_SHORT).show();
                });
    }
}