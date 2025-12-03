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
 * After login or registration, a corresponding user profile document is
 * created or updated in Firestore, and the user is forwarded to
 * {@link RoleSelectionActivity}.
 * </p>
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
     * Creates a corresponding user profile document in the Firestore users collection.
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
     * Navigates to the RoleSelectionActivity.
     */
    private void goToRoleSelection() {
        Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}