package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Main entry point for the Admin role.
 * This activity's only job is to launch the main Admin Dashboard.
 */
public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Immediately launch the real dashboard
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);

        // Finish this activity so the user can't navigate back to it
        finish();
    }
}