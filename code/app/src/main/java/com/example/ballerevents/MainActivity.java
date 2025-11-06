package com.example.ballerevents;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For prototype: go directly to Organizer profile About screen
        startActivity(new Intent(this, OrganizerProfileActivity.class));
        finish();
    }
}
