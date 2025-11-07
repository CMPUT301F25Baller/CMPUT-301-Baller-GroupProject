package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Import ViewBinding
import com.example.ballerevents.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Use ViewBinding
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already logged in: " + currentUser.getUid());
            goToRoleSelection();
        }

        // Access views via binding object
        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.btnRegister.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {
        // Access views via binding object
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
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        // Access views via binding object
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
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewUserDocument(FirebaseUser firebaseUser) {
        // Create a new user profile document in Firestore
        String userId = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        Map<String, Object> user = new HashMap<>();
        user.put("name", "New User"); // Default name
        user.put("email", email);
        user.put("aboutMe", "Tell us about yourself!");
        user.put("interests", new ArrayList<String>());
        user.put("appliedEventIds", new ArrayList<String>());
        user.put("profilePictureUrl", ""); // Default empty URL
        // role field will be set in RoleSelectionActivity

        db.collection("users").document(userId)
                .set(user, SetOptions.merge()) // SetOptions.merge() prevents overwriting if doc exists
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "New user document created for " + userId);
                    goToRoleSelection(); // Go to role selection after doc is created
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error creating user document", e);
                    Toast.makeText(LoginActivity.this, "Error setting up profile.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void goToRoleSelection() {
        Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
        startActivity(intent);
        finish(); // Prevent user from going back to login
    }
}