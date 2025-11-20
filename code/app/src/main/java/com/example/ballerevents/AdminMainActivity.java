package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Entry activity for the Admin role.
 *
 * <p>This activity exists solely as a redirector. When an admin logs in or
 * selects the admin role, the app launches this activity first. Its only
 * responsibility is to immediately start the
 * {@link AdminDashboardActivity}, which is the real admin home screen.
 *
 * <p>Once the dashboard is launched, this activity finishes itself so that
 * the user cannot accidentally navigate back to it using the back button.
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
