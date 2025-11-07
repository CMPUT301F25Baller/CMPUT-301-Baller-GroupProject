package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        Button btnOrganizer = findViewById(R.id.btnOrganizer);
        Button btnEntrant   = findViewById(R.id.btnEntrant);
        Button btnAdmin     = findViewById(R.id.btnAdmin);

        btnOrganizer.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectionActivity.this, OrganizerActivity.class))
        );

        btnEntrant.setOnClickListener(v ->
                startActivity(new Intent(RoleSelectionActivity.this, EntrantMainActivity.class))
        );

        btnAdmin.setOnClickListener(v ->
                Toast.makeText(this, "Admin prototype not wired yet.", Toast.LENGTH_SHORT).show()
        );
    }
}
