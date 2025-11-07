package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RoleSelectionActivity extends AppCompatActivity {

    private static final String TAG = "RoleSelectionActivity";

    // Flip to true if you want users with an existing role to skip this screen.
    private static final boolean AUTO_REDIRECT_IF_ROLE_SET = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1) Require login first
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2) (Optional) If role is already set, skip selection
        if (AUTO_REDIRECT_IF_ROLE_SET) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(this::maybeAutoRoute)
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to read user for role check", e));
        }

        // 3) Wire up buttons
        Button btnOrganizer = findViewById(R.id.btnOrganizer);
        Button btnEntrant   = findViewById(R.id.btnEntrant);
        Button btnAdmin     = findViewById(R.id.btnAdmin);

        btnOrganizer.setOnClickListener(v -> selectRole("organizer", OrganizerActivity.class));
        btnEntrant.setOnClickListener(v -> selectRole("entrant", EntrantMainActivity.class));
        btnAdmin.setOnClickListener(v -> selectRole("admin", AdminMainActivity.class));
    }

    private void maybeAutoRoute(DocumentSnapshot doc) {
        if (!AUTO_REDIRECT_IF_ROLE_SET) return;
        if (!doc.exists()) return;

        String role = doc.getString("role");
        if (role == null || role.isEmpty()) return;

        switch (role) {
            case "organizer":
                startActivity(new Intent(this, OrganizerActivity.class));
                finish();
                break;
            case "entrant":
                startActivity(new Intent(this, EntrantMainActivity.class));
                finish();
                break;
            case "admin":
                startActivity(new Intent(this, AdminMainActivity.class));
                finish();
                break;
            default:
                // Unknown role; stay on selection screen
                break;
        }
    }

    private void selectRole(String role, Class<?> activityClass) {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .update("role", role)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Role set: " + role);
                    startActivity(new Intent(RoleSelectionActivity.this, activityClass));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error setting role", e);
                    Toast.makeText(this, "Error setting role.", Toast.LENGTH_SHORT).show();
                });
    }
}
