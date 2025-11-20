package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ballerevents.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Launch activity responsible for handling user authentication.
 * <p>
 * Supports both sign-in and registration using Firebase Authentication.
 * After login or registration, a corresponding user profile document may be
 * created in Firestore, and the user is forwarded to
 * {@link RoleSelectionActivity}.
 * </p>
 */
public class LoginActivity extends AppCompatActivity {

    /** Logging tag for debugging authentication flow. */
    private static final String TAG = "LoginActivity";

    /** Firebase Authentication instance used for login/registration. */
    private FirebaseAuth mAuth;

    /** Firestore instance for storing user profile documents. */
    private FirebaseFirestore db;

    /** ViewBinding instance for accessing UI elements from activity_login.xml. */
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // If a user is already logged in, skip login screen
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already logged in: " + currentUser.getUid());
            goToRoleSelection();
        }

        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.btnRegister.setOnClickListener(v -> registerUser());
    }

    /**
     * Attempts to sign in an existing user using Firebase Authentication.
     * <p>
     * Email and password are read from the corresponding ViewBinding input fields.
     * Displays a Toast message on failure and navigates to {@link RoleSelectionActivity}
     * on success.
     * </p>
     */
    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        goToRoleSelection();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Creates a new Firebase Authentication user using an email and password.
     * <p>
     * On success, a new Firestore user document is created by calling
     * {@link #createNewUserDocument(FirebaseUser)}.
     * On failure, displays the full Firebase error message to the user.
     * </p>
     */
    private void registerUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            createNewUserDocument(user);
                        }
                    } else {
                        // Show detailed Firebase error
                        String errorMessage = "Registration failed.";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }

                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Creates a corresponding user profile document in the Firestore
     * {@code users} collection after user registration.
     * <p>
     * Default values are assigned for new fields such as name, aboutMe,
     * interests, applied events, and profile picture URL. The {@code role} field
     * is assigned later by {@link RoleSelectionActivity}.
     * </p>
     *
     * @param firebaseUser The authenticated user whose Firestore profile is created.
     */
    private void createNewUserDocument(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        Map<String, Object> user = new HashMap<>();
        user.put("name", "New User");
        user.put("email", email);
        user.put("aboutMe", "Tell us about yourself!");
        user.put("interests", new ArrayList<String>());
        user.put("appliedEventIds", new ArrayList<String>());
        user.put("profilePictureUrl", "");

        db.collection("users").document(userId)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New user document created for " + userId);
                    goToRoleSelection();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error creating user document", e);
                    Toast.makeText(LoginActivity.this,
                            "Error setting up profile.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigates to {@link RoleSelectionActivity} after authentication or profile creation.
     * <p>
     * This activity is finished to prevent returning to the login screen with the
     * back button.
     * </p>
     */
    private void goToRoleSelection() {
        Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}
