package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect to the admin dashboard and remove MainActivity from back stack
        startActivity(new Intent(this, AdminDashboardActivity.class));
        finish();
    }
}