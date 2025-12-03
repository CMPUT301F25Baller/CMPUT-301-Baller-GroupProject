package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry activity for the Admin role.
 *
 * <p>This activity acts as a router. When an admin logs in, this activity is launched first,
 * which then immediately redirects to the {@link AdminDashboardActivity}.
 * It finishes itself immediately to prevent navigation back to this intermediate state.</p>
 */
public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch the main dashboard for admin users.
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);

        // Prevent returning to this launcher activity.
        finish();
    }
}