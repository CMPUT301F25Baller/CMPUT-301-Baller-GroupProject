package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        Button btnEntrant = findViewById(R.id.btnEntrant);
        Button btnOrganizer = findViewById(R.id.btnOrganizer);
        Button btnAdmin = findViewById(R.id.btnAdmin);

        btnEntrant.setOnClickListener(v -> {
            Intent i = new Intent(this, EntrantMainActivity.class);
            startActivity(i);
        });

        btnOrganizer.setOnClickListener(v -> {
            Intent i = new Intent(this, OrganizerProfileActivity.class);
            startActivity(i);
        });

        btnAdmin.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminMainActivity.class);
            startActivity(i);
        });
    }
}
